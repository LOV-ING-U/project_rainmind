package com.project.rainmind.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "생성된 schedule id")
data class ScheduleCreateResponse (
    val scheduleId: Long
)