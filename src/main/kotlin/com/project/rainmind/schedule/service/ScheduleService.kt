package com.project.rainmind.schedule.service

import com.project.rainmind.alarm.NotifyQueueService
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
import java.time.ZoneId

@Service
class ScheduleService (
    private val scheduleRepository: ScheduleRepository,
    private val locationRepository: LocationRepository,
    private val userLogInRepository: UserLogInRepository,
    private val notifyQueueService: NotifyQueueService
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

        val locationExists = locationRepository.existsById(locationId)
        if(!locationExists) throw InvalidRegionNameException()

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

        // register alarm at redis
        // 중간에 실패하면 이건 어떻게 됨?
        // 지정된 곳의 로컬 시간대 = notifyAt
        val alarmAt = save.startAt.minusMinutes(30)
        val notifyAt = alarmAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        notifyQueueService.enqueueAlarm(save.id!!, notifyAt)

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

        // dequeue
        notifyQueueService.dequeueAlarm(schedule.id!!)

        return ScheduleDeleteResponse(
            deletedScheduleId = schedule.id!!
        )
    }
}