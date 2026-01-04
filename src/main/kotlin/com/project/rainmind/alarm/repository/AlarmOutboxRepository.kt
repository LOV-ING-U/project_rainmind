package com.project.rainmind.alarm.repository

import com.project.rainmind.alarm.AlarmOutboxStatus
import com.project.rainmind.alarm.entity.AlarmOutbox
import org.springframework.data.repository.CrudRepository

interface AlarmOutboxRepository : CrudRepository<AlarmOutbox, Long>{
    fun findByStatus(
        status: AlarmOutboxStatus
    ): List<AlarmOutbox>

    fun findAllByScheduleId(
        scheduleId: Long
    ): List<AlarmOutbox>

    fun existsByScheduleId(scheduleId: Long): Boolean
}