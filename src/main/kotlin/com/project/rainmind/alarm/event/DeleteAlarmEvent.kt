package com.project.rainmind.alarm.event

data class DeleteAlarmEvent (
    // user별 키 분산을 위한 userId추가
    val userId: Long,
    val scheduleId: Long,
    val payload: String
)
