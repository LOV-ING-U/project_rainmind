package com.project.rainmind.alarm.event

data class DeleteAlarmEvent (
    // ---- new ---- 
   // val userId: Long,
   // val scheduleId: Long
    val scheduleId: Long,
    val payload: String
)
