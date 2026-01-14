package com.project.rainmind.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "schedule 생성 요청")
data class ScheduleCreateRequest (
    @Schema(description = "schedule 이름")
    @field:NotBlank(message = "title required")
    var title: String,

    @Schema(description = "schedule 장소")
    @field:NotNull(message = "location id required")
    var locationId: Long?,

    @Schema(description = "schedule 시작 시각")
    @field:NotNull(message = "startAt must be future")
    var startAt: LocalDateTime?,

    @Schema(description = "schedule 종료 시각")
    @field:NotNull(message = "endAt must be future")
    var endAt: LocalDateTime?
)