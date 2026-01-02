package com.project.rainmind.alarm

import com.project.rainmind.schedule.repository.ScheduleRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class NotifyAlarmWorker (
    private val notifyQueueService: NotifyQueueService,
    private val scheduleRepository: ScheduleRepository
) {
    // print alarm
    // 60000ms 마다 실행되게 함
    // 일반적 비즈니스 로직은 http 요청이 있을때 처리되지만 알람은 그렇지않다. 따라서 worker 따로 만듬
    @Scheduled(fixedDelay = 60000)
    fun sendAlarm() {
        val nowMs = Instant.now().toEpochMilli()
        val deque = notifyQueueService.popAll(nowMs)

        if(deque.isEmpty()) return

        // DB에서는 삭제되었는데 redis에서는 삭제 안된 경우 방지
        // 그렇다면 DB에는 추가되었는데 redis에 추가가 안된 경우는?
        // cache read 전략에 대해 고민해보자..
        deque.forEach { id ->
            val schedule = scheduleRepository.findById(id).orElse(null)
            if(schedule != null) println("[Notify schedule before 30 minutes] schedule id = " + schedule.id + ", user id = " + schedule.userId + ", title = " + schedule.title)
        }
    }
}