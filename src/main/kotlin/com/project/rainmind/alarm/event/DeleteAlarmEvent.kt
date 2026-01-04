package com.project.rainmind.alarm.event

data class DeleteAlarmEvent (
    val scheduleId: Long,
    val payload: String
)