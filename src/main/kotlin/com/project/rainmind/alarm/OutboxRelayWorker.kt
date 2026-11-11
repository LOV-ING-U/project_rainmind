package com.project.rainmind.alarm

import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import com.project.rainmind.alarm.service.NotifyQueueService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private const val BATCH_SIZE = 1000

// outbox -> Redis 큐 등록. 알림 등록의 유일한 경로
@Component
class OutboxRelayWorker (
    private val alarmOutboxRepository: AlarmOutboxRepository,
    private val notifyQueueService: NotifyQueueService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10000)
    @SchedulerLock(name = "outboxRelayLock", lockAtMostFor = "PT30S")
    fun relay() {
        relayOnce()
    }

    // 등록에 성공한 outbox 만 삭제한다. 실패한 행은 남아서 다음 주기에 재시도된다.
    // 같은 id 가 두 번 등록돼도 ZADD 는 멱등이라 문제없다.
    fun relayOnce(): Int {
        val batch = alarmOutboxRepository.findBatch(BATCH_SIZE)
        val enqueued = mutableListOf<Long>()

        for (outbox in batch) {
            try {
                notifyQueueService.enqueue(outbox.scheduleId, outbox.alarmAt)
                enqueued += outbox.id!!
            } catch (e: Exception) {
                log.warn("outbox 등록 실패 (outboxId={}): {}", outbox.id, e.message)
            }
        }

        if (enqueued.isNotEmpty()) alarmOutboxRepository.deleteAllById(enqueued)
        return enqueued.size
    }
}
