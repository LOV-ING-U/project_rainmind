package com.project.rainmind.schedule.repository

import com.project.rainmind.schedule.entity.Schedule
import org.springframework.data.repository.ListCrudRepository
import java.util.*

interface ScheduleRepository : ListCrudRepository<Schedule, Long> {
    fun findByIdAndUserId(
        id: Long,
        userId: Long
    ): Schedule?

    override fun deleteById(
        id: Long
    )
}