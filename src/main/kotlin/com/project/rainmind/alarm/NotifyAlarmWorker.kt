package com.project.rainmind.alarm

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.service.NotifyQueueService
import com.project.rainmind.schedule.repository.ScheduleRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class NotifyAlarmWorker (
    private val notifyQueueService: NotifyQueueService,
    private val scheduleRepository: ScheduleRepository,
    private val objectMapper: ObjectMapper
) {
    // print alarm
    // 60000ms 마다 실행되게 함
    // 일반적 비즈니스 로직은 http 요청이 있을때 처리되지만 알람은 그렇지않다. 따라서 worker 따로 만듬
    @Scheduled(fixedDelay = 60000)
    fun sendAlarm() {
        val alarm = notifyQueueService.dequeue() ?: return

        val payload = objectMapper.readValue(alarm, NotifyAlarmPayload::class.java)
        val schedule = scheduleRepository.findById(payload.scheduleId)

        if(schedule.isEmpty) return
        println("[Notify alarm before 30 minutes] : start time = ${schedule.get().startAt}, title = ${schedule.get().title}")
    }

    // only for testing
    fun runOnceOnTest() = sendAlarm()
}