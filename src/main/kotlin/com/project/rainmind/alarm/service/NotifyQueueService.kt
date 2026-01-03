package com.project.rainmind.alarm.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.AlarmOutboxStatus
import com.project.rainmind.alarm.NotifyAlarmPayload
import com.project.rainmind.alarm.entity.AlarmOutbox
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import org.springframework.scheduling.annotation.Scheduled
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
    private val ZSET_KEY = "alarm:queue"

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun enqueueAfterCommit(
        outbox: AlarmOutbox
    ) {
        try {
            val payload = outbox.payload
            val notifyAlarmPayload = objectMapper.readValue(payload, NotifyAlarmPayload::class.java)
            val score = notifyAlarmPayload.alarmAt.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli().toDouble()

            stringRedisTemplate.opsForZSet().add(ZSET_KEY, payload, score)

            // enqueue 성공
            outbox.status = AlarmOutboxStatus.SENT
            alarmOutboxRepository.save(outbox)
        } catch (e: Exception) { } // 실패
    }

    // redis 에서 알람 1개(payload)를 꺼내서 반환(String 타입)
    // 단, 현재 호출하는 시각 이전 알람이어야 함
    // 삭제는 원자적이지 못함. 따라서 Lua script 등을 사용해야함.
    fun dequeue(): String? {
        val nowTime = System.currentTimeMillis().toDouble()

        // dequeue
        // 최소/최대 이상/이하(경계 포함) 내에서, offset(index 번호)부터 count 개수만큼 조회
        val alarm = stringRedisTemplate.opsForZSet().rangeByScore(ZSET_KEY, 0.0, nowTime, 0, 1)?.firstOrNull()
        if(alarm == null) return null
        else {
            stringRedisTemplate.opsForZSet().remove(ZSET_KEY, alarm)
            return alarm
        }
    }

    // commit 이후 redis 에 enqueue 실패시 남은 PENDING 들을 성공할때까지 재시도
    // enqueue 원자성 해결, dequeue 원자성 미해결
    @Scheduled(fixedDelay = 10000)
    fun retryPending() {
        val pendings = alarmOutboxRepository.findByStatus(AlarmOutboxStatus.PENDING)

        // for each pending signals,
        for(signal in pendings) {
            try {
                val payload = signal.payload
                val notifyAlarmPayload = objectMapper.readValue(payload, NotifyAlarmPayload::class.java)
                val score = notifyAlarmPayload.alarmAt.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli().toDouble()

                stringRedisTemplate.opsForZSet().add(ZSET_KEY, payload, score)

                // enqueue 성공
                signal.status = AlarmOutboxStatus.SENT
                alarmOutboxRepository.save(signal)
            } catch (e: Exception) { }
        }
    }
}