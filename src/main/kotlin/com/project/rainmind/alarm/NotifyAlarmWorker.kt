package com.project.rainmind.alarm

import com.project.rainmind.alarm.sender.AlarmMessage
import com.project.rainmind.alarm.sender.AlarmSender
import com.project.rainmind.alarm.service.NotifyQueueService
import com.project.rainmind.common.SEOUL
import com.project.rainmind.schedule.ScheduleRepository
import com.project.rainmind.weather.WeatherStore
import io.micrometer.core.instrument.Metrics
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val BATCH_SIZE = 2000

// 전제: Redis 큐에서 꺼낸(pop) 알림은 반드시 발송된다고 가정한다.
// 이 가정을 없애려면 pop 과 Redis Streams XADD 를 묶고 ACK 기반으로 발송한다(README 확장 설계 참고).
@Component
class NotifyAlarmWorker (
    private val notifyQueueService: NotifyQueueService,
    private val scheduleRepository: ScheduleRepository,
    private val alarmSender: AlarmSender,
    private val weatherStore: WeatherStore
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val totalProcessTimer = Metrics.timer("method_timed", "method", "total_process")
    private val dequeueTimer = Metrics.timer("method_timed", "method", "pure_dequeue")

    @Scheduled(fixedDelay = 2000)
    @SchedulerLock(
        name = "notifyAlarmWorkerLock",
        lockAtMostFor = "PT10S",
        lockAtLeastFor = "PT1S"
    )
    fun sendAlarm() {
        sendOnce()
    }

    // 반환값: 실제로 발송한 알림 수
    fun sendOnce(): Int {
        val startTime = System.nanoTime()
        val ids = notifyQueueService.popDue(BATCH_SIZE)
        dequeueTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
        if (ids.isEmpty()) return 0

        // 존재하고, 아직 안 보냈고, 시작 전인 일정만 남는다.
        // 삭제된 일정(유령), 이미 보낸 일정(중복), 장애로 늦어 시작이 지난 일정은 여기서 걸러진다.
        val now = LocalDateTime.now(SEOUL)
        val schedules = scheduleRepository.findSendable(ids, now)

        val sent = mutableListOf<Long>()
        for (schedule in schedules) {
            try {
                alarmSender.send(
                    AlarmMessage(
                        scheduleId = schedule.id!!,
                        userId = schedule.userId,
                        title = schedule.title,
                        startAt = schedule.startAt,
                        regionName = schedule.regionName,
                        weather = weatherStore.find(schedule.nx, schedule.ny, schedule.startAt)
                    )
                )
                sent += schedule.id!!
            } catch (e: Exception) {
                log.warn("알림 발송 실패 (scheduleId={}): {}", schedule.id, e.message)
            }
        }

        if (sent.isNotEmpty()) scheduleRepository.markNotified(sent, now)

        totalProcessTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
        return sent.size
    }
}
