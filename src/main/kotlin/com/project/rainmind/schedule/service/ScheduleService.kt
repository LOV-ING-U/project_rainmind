package com.project.rainmind.schedule.service

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

@Service
class ScheduleService (
    private val scheduleRepository: ScheduleRepository,
    private val locationRepository: LocationRepository,
    private val userLogInRepository: UserLogInRepository
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

        return ScheduleDeleteResponse(
            deletedScheduleId = schedule.id!!
        )
    }
}