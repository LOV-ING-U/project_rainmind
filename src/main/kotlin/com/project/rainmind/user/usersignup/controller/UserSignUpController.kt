package com.project.rainmind.user.usersignup.controller

import com.project.rainmind.user.dto.UserSignUpRequest
import com.project.rainmind.user.dto.UserSignUpResponse
import com.project.rainmind.user.usersignup.service.UserSignUpService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserSignUpController (
    @Autowired
    private val userSignUpService: UserSignUpService,
){
    @PostMapping("/v1/auth/register")
    fun register(
        @RequestBody userSignUpRequest: UserSignUpRequest
    ): ResponseEntity<UserSignUpResponse> {
        val userSignUpResponse = userSignUpService
            .register(
                nickname = userSignUpRequest.nickname,
                password = userSignUpRequest.password,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(userSignUpResponse)
    }

}