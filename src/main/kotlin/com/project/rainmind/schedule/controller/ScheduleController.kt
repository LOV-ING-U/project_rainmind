package com.project.rainmind.schedule.controller

import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import com.project.rainmind.schedule.dto.ScheduleDeleteResponse
import com.project.rainmind.schedule.service.ScheduleService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/schedules")
class ScheduleController (
    private val scheduleService: ScheduleService
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal nickname: String,
        @RequestBody scheduleCreateRequest: ScheduleCreateRequest
    ): ResponseEntity<ScheduleCreateResponse> {

    }

    @DeleteMapping("/{scheduleId}")
    fun delete(
        @AuthenticationPrincipal nickname: String,
        @PathVariable scheduleId: Long
    ): ResponseEntity<ScheduleDeleteResponse> {

    }
}