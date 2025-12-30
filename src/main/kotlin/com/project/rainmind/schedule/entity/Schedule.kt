package com.project.rainmind.schedule.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "schedules")
class Schedule (
    @Id
    var id: Long? = null,
    @Column("user_id")
    var userId: Long,
    @Column("title")
    var title: String,
    @Column("location_id")
    var locationId: Long,
    @Column("start_at")
    var startAt: LocalDateTime,
    @Column("end_at")
    var endAt: LocalDateTime
)