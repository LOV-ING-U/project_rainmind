package com.project.rainmind.user.login.controller

import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.login.service.UserLogInService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserLogInController (
    @Autowired
    private val userLogInService: UserLogInService
){
    @PostMapping("/v1/auth/login")
    fun login(
        userLogInRequest: UserLogInRequest
    ): ResponseEntity<UserLogInResponse> {
        val userLogInResponse = userLogInService.login(
            nickname = userLogInRequest.nickname,
            password = userLogInRequest.password
        )

        return ResponseEntity.ok(userLogInResponse)
    }
}