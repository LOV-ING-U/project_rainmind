package com.project.rainmind.schedule.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.AlarmOutboxStatus
import com.project.rainmind.alarm.NotifyAlarmPayload
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import com.project.rainmind.schedule.InvalidScheduleStartAndEndTimeException
import com.project.rainmind.schedule.ScheduleNotFoundException
import com.project.rainmind.schedule.TooManySchedulesException
import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import com.project.rainmind.schedule.dto.ScheduleDeleteResponse
import com.project.rainmind.schedule.entity.Schedule
import com.project.rainmind.schedule.repository.ScheduleRepository
import com.project.rainmind.user.NonExistingUsernameException
import com.project.rainmind.user.repository.UserLogInRepository
import com.project.rainmind.weather.InvalidRegionNameException
import com.project.rainmind.weather.repository.LocationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.project.rainmind.alarm.entity.AlarmOutbox
import com.project.rainmind.alarm.event.DeleteAlarmEvent
import org.springframework.context.ApplicationEventPublisher

@Service
class ScheduleService (
    private val scheduleRepository: ScheduleRepository,
    private val locationRepository: LocationRepository,
    private val userLogInRepository: UserLogInRepository,
    private val alarmOutboxRepository: AlarmOutboxRepository,
    private val objectMapper: ObjectMapper,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun createSchedule(
        nickname: String,
        title: String,
        locationId: Long,
        startAt: LocalDateTime,
        endAt: LocalDateTime
    ): ScheduleCreateResponse {
        val user = userLogInRepository.findByNickname(nickname) ?: throw NonExistingUsernameException()

        val location = locationRepository.findById(locationId).orElse(null) ?: throw InvalidRegionNameException()

        if(scheduleRepository.findAll().size >= 30) throw TooManySchedulesException()
        if(startAt.isAfter(endAt)) throw InvalidScheduleStartAndEndTimeException()

        val save = scheduleRepository.save(
            Schedule(
                userId = user.id!!,
                title = title,
                locationId = locationId,
                startAt = startAt,
                endAt = endAt
            )
        )

        // outbox DB에 등록
        val alarmAt = save.startAt.minusMinutes(30)
        val payload = NotifyAlarmPayload(
            scheduleId = save.id!!,
            userId = user.id!!,
            title = title,
            regionName = location.regionName,
            nx = location.nx,
            ny = location.ny,
            startAt = save.startAt,
            alarmAt = alarmAt
        )

        val savedOutbox = alarmOutboxRepository.save(
            AlarmOutbox(
                scheduleId = save.id!!,
                payload = objectMapper.writeValueAsString(payload),
                status = AlarmOutboxStatus.PENDING,
            )
        )

        applicationEventPublisher.publishEvent(savedOutbox)
        return ScheduleCreateResponse(
            scheduleId = save.id!!
        )
    }

    @Transactional
    fun deleteSchedule(
        nickname: String,
        scheduleId: Long
    ): ScheduleDeleteResponse {
        val user = userLogInRepository.findByNickname(nickname) ?: throw NonExistingUsernameException()

        val schedule = scheduleRepository.findByIdAndUserId(scheduleId, user.id!!) ?: throw ScheduleNotFoundException()
        scheduleRepository.deleteById(schedule.id!!)

        // delete at outbox(deleted)
        val outboxes = alarmOutboxRepository.findAllByScheduleId(schedule.id!!)
        for(outbox in outboxes) {
            outbox.status = AlarmOutboxStatus.DELETED
            alarmOutboxRepository.save(outbox)

            applicationEventPublisher.publishEvent(
                DeleteAlarmEvent(
                    scheduleId = schedule.id!!,
                    payload = outbox.payload
                )
            )
        }

        return ScheduleDeleteResponse(
            deletedScheduleId = schedule.id!!
        )
    }
}