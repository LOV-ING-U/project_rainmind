package com.project.rainmind.alarm.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.AlarmOutboxStatus
import com.project.rainmind.alarm.NotifyAlarmPayload
import com.project.rainmind.alarm.entity.AlarmOutbox
import com.project.rainmind.alarm.event.DeleteAlarmEvent
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.ZoneId

@Service
class NotifyQueueService (
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val alarmOutboxRepository: AlarmOutboxRepository
) {
    // redis zset 사용
    // key: 알림 모음(= alarm:at 고정)
    // value: 알림들 집합(각 알람들은 이름/score 로 구성됨)
    // score 정렬된 상태로 유지(sorted set)
    // deque 원자성 위한 lua script 도입
    private val ZSET_KEY = "alarm:queue"
    private var scriptLua = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local count = tonumber(ARGV[2])            

            local items = redis.call('ZRANGEBYSCORE', key, '-inf', now, 'LIMIT', 0, count)
            if (#items == 0) then
                return nil
            end
            
            redis.call('ZREM', key, unpack(items))
            return items
        """.trimIndent()
    private val deqSyncLua: DefaultRedisScript<List<*>> = DefaultRedisScript<List<*>>().apply {
        resultType = List::class.java
        setScriptText(scriptLua)
    }

    // @Async?
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun enqueueAfterCommit(
        outbox: AlarmOutbox
    ) {
        try {
            val payload = outbox.payload
            val notifyAlarmPayload = objectMapper.readValue(payload, NotifyAlarmPayload::class.java)
            val score = notifyAlarmPayload.alarmAt.toEpochMilli().toDouble()

            stringRedisTemplate.opsForZSet().add(userZSetKey(notifyAlarmPayload.userId), payload, score)

            // enqueue 성공
            // outbox.status = AlarmOutboxStatus.SENT
            alarmOutboxRepository.delete(outbox)
        } catch (e: Exception) { } // 실패
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun removeAfterCommit(
        event: DeleteAlarmEvent
    ) {
        try {
            stringRedisTemplate.opsForZSet().remove(userZSetKey(event.userId), event.payload)
        } catch (e: Exception) { } // 실패
    }

    // redis 에서 알람 1개(payload)를 꺼내서 반환(String 타입)
    // 단, 현재 호출하는 시각 이전 알람이어야 함
    // 삭제는 원자적이지 못함. 따라서 Lua script 등을 사용해야함.
    fun dequeue(
        count: Int = 2000
    ): List<String> {
        val nowTime = System.currentTimeMillis().toDouble().toString()

        val result = stringRedisTemplate.execute(
            deqSyncLua,
            listOf(ZSET_KEY),
            nowTime,
            count.toString()
        )

        return (result as? List<String>) ?: emptyList()
    }

    // commit 이후 redis 에 enqueue 실패시 남은 PENDING 들을 성공할때까지 재시도
    // @Scheduled(fixedDelay = 10000)
    // 그냥 떼고 scheduler 로 옮긴다. 서비스 레이어 책임이 흐려지는 느낌...
    fun retryPending() {
        val pendings = alarmOutboxRepository.findByStatus(AlarmOutboxStatus.PENDING)

        // for each pending signals,
        for(signal in pendings) {
            try {
                val payload = signal.payload
                val notifyAlarmPayload = objectMapper.readValue(payload, NotifyAlarmPayload::class.java)
                val score = notifyAlarmPayload.alarmAt.toEpochMilli().toDouble()

                stringRedisTemplate.opsForZSet().add(userZSetKey(notifyAlarmPayload.userId), payload, score)

                // enqueue 성공
                // signal.status = AlarmOutboxStatus.SENT
                alarmOutboxRepository.delete(signal)
            } catch (e: Exception) { }
        }
    }

    private fun userZSetKey(
        userId: Long
    ): String = "alarm:queue:u:{$userId}"



    // user별 key 추가를 위한 새로운 함수
    // 1. redis 키를 순회하여, alarm:queue:u:*형태의 key들을 싹 다 모은다.
    fun findAllUserQueueKeys(): Set<String> {
        return stringRedisTemplate.keys("alarm:queue:u:*") ?: emptySet()
    }

    // 2. 특정 key에서, 1개씩 가져온다.
    fun dequeueFromUserQueue(
        queueKey: String,
        count: Int = 1
    ): List<String> {
        val nowTime = System.currentTimeMillis().toString()
 
        val result = stringRedisTemplate.execute(
            deqSyncLua,
            listOf(queueKey),
            nowTime,
            count.toString()
        )

        return (result as? List<String>) ?: emptyList()
    }

    // 3. 모든 user의 key에 대해 1개씩 가져온다.
    fun dequeueFromAllUsers(
        count: Int = 1
    ): List<String> {
        val queueKey = findAllUserQueueKeys()

        if(queueKey.isEmpty()) return emptyList()

        val ret = mutableListOf<String>()
        for(queuekey in queueKey){
            val dequeuedAlarm = dequeueFromUserQueue(queuekey, count)
            if(!dequeuedAlarm.isEmpty()) ret.addAll(dequeuedAlarm)
        }

        return ret
    }
}
