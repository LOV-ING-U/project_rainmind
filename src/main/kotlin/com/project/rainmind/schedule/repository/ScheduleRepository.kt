package com.project.rainmind.schedule.repository

import com.project.rainmind.schedule.entity.Schedule
import org.springframework.data.repository.ListCrudRepository
import java.util.*
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query

interface ScheduleRepository : ListCrudRepository<Schedule, Long> {
    fun findByIdAndUserId(
        id: Long,
        userId: Long
    ): Schedule?

    override fun deleteById(
        id: Long
    )

    fun findAllByUserId(
        userId: Long
    ): List<Schedule>

    fun countByUserId(
        userId: Long
    ): Long

    @Modifying
    @Transactional
    @Query(value = """INSERT INTO schedules (user_id, title, location_id, start_at, end_at) 
        SELECT :userId, :title, :locationId, :start_at, :end_at
        WHERE (SELECT COUNT(*) FROM schedules WHERE user_id = :userId) <= 29
    """)
    fun executeInsertWhenUnderLimit(
        @Param("userId") userId: Long,
        @Param("title") title: String,
        @Param("locationId") locationId: Long,
        @Param("start_at") startAt: LocalDateTime,
        @Param("end_at") endAt: LocalDateTime
    ): Int

    @Query(value = """SELECT LAST_INSERT_ID()""")
    fun getLastInsertId(): Long
}
