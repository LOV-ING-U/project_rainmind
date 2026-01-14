package com.project.rainmind.schedule.controller

import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import com.project.rainmind.schedule.dto.ScheduleDeleteResponse
import com.project.rainmind.schedule.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
        // 개인 일정은 토큰의 주인만 접근해야한다.
        @AuthenticationPrincipal nickname: String,
        @RequestBody @Valid scheduleCreateRequest: ScheduleCreateRequest
    ): ResponseEntity<ScheduleCreateResponse> {
        val response = scheduleService.createSchedule(nickname, scheduleCreateRequest.title, scheduleCreateRequest.locationId!!, scheduleCreateRequest.startAt!!, scheduleCreateRequest.endAt!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{scheduleId}")
    fun delete(
        // 개인 일정은 토큰의 주인만 접근해야한다.
        @AuthenticationPrincipal nickname: String,
        @PathVariable scheduleId: Long
    ): ResponseEntity<ScheduleDeleteResponse> {
        val response = scheduleService.deleteSchedule(nickname, scheduleId)
        return ResponseEntity.ok(response)
    }
}