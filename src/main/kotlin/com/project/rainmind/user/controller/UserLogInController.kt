package com.project.rainmind.user.controller

import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.service.UserLogInService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserLogInController (
    private val userLogInService: UserLogInService
){
    @PostMapping("/v1/auth/user/login")
    fun login(
        @RequestBody userLogInRequest: UserLogInRequest
    ): ResponseEntity<UserLogInResponse> {
        val userLogInResponse = userLogInService.login(
            nickname = userLogInRequest.nickname,
            password = userLogInRequest.password
        )

        return ResponseEntity.ok(userLogInResponse)
    }
}