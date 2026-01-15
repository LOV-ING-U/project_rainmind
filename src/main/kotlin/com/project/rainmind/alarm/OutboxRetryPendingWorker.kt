package com.project.rainmind.alarm

import com.project.rainmind.alarm.service.NotifyQueueService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxRetryPendingWorker (
    private val notifyQueueService: NotifyQueueService
) {
    @Scheduled(fixedDelay = 10000) // 10초마다
    @SchedulerLock(
        name = "outboxRetryPendingLock",
        lockAtMostFor = "PT30S" // 너무 짧게 설정하면,, process 하나가 redis 삽입하다가 락 풀리면
        // 또 다른 process 가 동일한 알람을 redis 삽입할 수도 있으니 넉넉하게...
    )
    fun retryPendingAlarm() {
        notifyQueueService.retryPending()
    }
}