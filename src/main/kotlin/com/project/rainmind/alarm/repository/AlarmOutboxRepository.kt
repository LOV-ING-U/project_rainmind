package com.project.rainmind.alarm.repository

import com.project.rainmind.alarm.entity.AlarmOutbox
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface AlarmOutboxRepository : ListCrudRepository<AlarmOutbox, Long> {
    // PK 순으로 limit 개
    @Query("SELECT * FROM alarm_outbox ORDER BY id LIMIT :limit")
    fun findBatch(
        @Param("limit") limit: Int
    ): List<AlarmOutbox>

    fun findAllByScheduleId(
        scheduleId: Long
    ): List<AlarmOutbox>

    @Modifying
    @Query("DELETE FROM alarm_outbox WHERE schedule_id = :scheduleId")
    fun deleteByScheduleId(
        @Param("scheduleId") scheduleId: Long
    ): Int
}
