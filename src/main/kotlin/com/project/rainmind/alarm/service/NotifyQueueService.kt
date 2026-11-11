package com.project.rainmind.alarm.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.AlarmOutboxStatus
import com.project.rainmind.alarm.NotifyAlarmPayload
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import org.springframework.data.redis.core.script.DefaultRedisScript
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

    // PENDING 상태의 outbox 를 redis 에 등록 (알림 등록의 유일한 경로)
    // 성공 시 SENT 로 변경, 실패 시 PENDING 유지 후 다음 주기에 재시도
    fun retryPending() {
        val pendings = alarmOutboxRepository.findByStatus(AlarmOutboxStatus.PENDING)

        // for each pending signals,
        for(signal in pendings) {
            try {
                val payload = signal.payload
                val notifyAlarmPayload = objectMapper.readValue(payload, NotifyAlarmPayload::class.java)
                val score = notifyAlarmPayload.alarmAt.toEpochMilli().toDouble()

                stringRedisTemplate.opsForZSet().add(ZSET_KEY, payload, score)

                // enqueue 성공
                signal.status = AlarmOutboxStatus.SENT
                alarmOutboxRepository.save(signal)
            } catch (e: Exception) { }
        }
    }

    private fun userZSetKey(
        userId: Long
    ): String = "alarm:queue:u:{$userId}"
}
