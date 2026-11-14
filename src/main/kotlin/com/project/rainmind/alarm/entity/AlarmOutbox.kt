package com.project.rainmind.alarm.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

// Redis 에 아직 등록되지 않은 알림. 등록에 성공하면 행을 삭제한다.
@Table(name = "alarm_outbox")
class AlarmOutbox (
    @Id
    var id: Long? = null,
    @Column("schedule_id")
    var scheduleId: Long,
    @Column("alarm_at")
    var alarmAt: LocalDateTime
)
