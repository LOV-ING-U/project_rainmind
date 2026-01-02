package com.project.rainmind.alarm

import java.time.LocalDateTime

data class NotifyAlarmPayload (
    val scheduleId: Long,
    val userId: Long,
    val title: String,
    val regionName: String,
    val nx: Int,
    val ny: Int,
    val startAt: LocalDateTime,
    val alarmAt: LocalDateTime
)