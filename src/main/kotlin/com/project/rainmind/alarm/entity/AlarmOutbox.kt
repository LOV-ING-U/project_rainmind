package com.project.rainmind.alarm.entity

import com.project.rainmind.alarm.AlarmOutboxStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "alarm_outbox")
class AlarmOutbox (
    @Id
    var id: Long? = null,
    @Column("schedule_id")
    var scheduleId: Long,
    @Column("payload")
    var payload: String,
    @Column("status")
    var status: AlarmOutboxStatus = AlarmOutboxStatus.PENDING,
    @Column("created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),
)