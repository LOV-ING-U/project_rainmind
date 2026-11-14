package com.project.rainmind.schedule.dto

import java.time.OffsetDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ScheduleCreateRequest (
    @field:NotBlank(message = "title required")
    var title: String,

    @field:NotBlank(message = "regionCode required")
    var regionCode: String,

    @field:NotNull(message = "startAt required")
    var startAt: OffsetDateTime?
)
