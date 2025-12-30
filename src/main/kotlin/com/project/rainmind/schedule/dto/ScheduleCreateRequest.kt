package com.project.rainmind.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "schedule 생성 요청")
data class ScheduleCreateRequest (
    @Schema(description = "schedule 이름")
    var title: String,
    @Schema(description = "schedule 장소")
    var locationId: Long,
    @Schema(description = "schedule 시작 시각")
    var startAt: LocalDateTime,
    @Schema(description = "schedule 종료 시각")
    var endAt: LocalDateTime
)