package com.project.rainmind.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "삭제된 schedule id")
data class ScheduleDeleteResponse (
    val deletedScheduleId: Long
)