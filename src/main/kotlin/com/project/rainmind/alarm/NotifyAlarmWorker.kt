package com.project.rainmind.alarm

import io.micrometer.core.instrument.Metrics
import java.util.concurrent.TimeUnit
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.service.NotifyQueueService
import com.project.rainmind.schedule.repository.ScheduleRepository
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotifyAlarmWorker (
    private val notifyQueueService: NotifyQueueService,
    private val scheduleRepository: ScheduleRepository,
    private val objectMapper: ObjectMapper
) {
    private val totalProcessTimer = Metrics.timer("method_timed", "method", "total_process")
    private val dequeueTimer = Metrics.timer("method_timed", "method", "pure_dequeue")
    // print alarm
    // 60000ms 마다 실행되게 함
    // 일반적 비즈니스 로직은 http 요청이 있을때 처리되지만 알람은 그렇지않다. 따라서 worker 따로 만듬
    @Scheduled(fixedDelay = 2000)
    @SchedulerLock(
        name = "notifyAlarmWorkerLock",
        lockAtMostFor = "PT10S",
        lockAtLeastFor = "PT1S"
    )
    fun sendAlarm() {
        val startTime = System.nanoTime()
        val alarms = notifyQueueService.dequeueFromAllUsers(1) ?: return
        val duration = System.nanoTime() - startTime

       // val payload = objectMapper.readValue(alarm, NotifyAlarmPayload::class.java)
       // val schedule = scheduleRepository.findById(payload.scheduleId)

        dequeueTimer.record(duration, TimeUnit.NANOSECONDS)

        try {
            val payloads = alarms.map { objectMapper.readValue(it, NotifyAlarmPayload::class.java) }
            val scheduleIds = payloads.map { it.scheduleId }

            val schedules = scheduleRepository.findAllById(scheduleIds)

            // send alarm
            /*
            
            */
        } catch (e: Exception) { }

        totalProcessTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)

      //  if(schedule.isEmpty) return
       // just for testing
      //  println("[Notify alarm before 30 minutes] : start time = ${schedule.get().startAt}, title = ${schedule.get().title}")
    }

    // only for testing
    fun runOnceOnTest() = sendAlarm()
}
