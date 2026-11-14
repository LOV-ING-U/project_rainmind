package com.project.rainmind.schedule

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ScheduleRepository : ListCrudRepository<Schedule, Long> {
    fun findByIdAndUserId(
        id: Long,
        userId: Long
    ): Schedule?

    fun countByUserId(
        userId: Long
    ): Long

    // 발송 대상: 존재하고, 아직 안 보냈고, 시작 전인 일정만 (PK 조회)
    @Query("SELECT * FROM schedules WHERE id IN (:ids) AND notified_at IS NULL AND start_at > :now")
    fun findSendable(
        @Param("ids") ids: Collection<Long>,
        @Param("now") now: LocalDateTime
    ): List<Schedule>

    @Modifying
    @Query("UPDATE schedules SET notified_at = :notifiedAt WHERE id IN (:ids)")
    fun markNotified(
        @Param("ids") ids: Collection<Long>,
        @Param("notifiedAt") notifiedAt: LocalDateTime
    ): Int
}
