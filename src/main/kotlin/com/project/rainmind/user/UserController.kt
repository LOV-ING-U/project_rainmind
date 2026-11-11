package com.project.rainmind.user

import com.project.rainmind.jwt.JwtTokenProvider
import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.dto.UserSignUpRequest
import com.project.rainmind.user.dto.UserSignUpResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController (
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/auth/user/register")
    fun register(
        @RequestBody userSignUpRequest: UserSignUpRequest
    ): ResponseEntity<UserSignUpResponse> {
        val userSignUpResponse = userService.register(
            nickname = userSignUpRequest.nickname,
            password = userSignUpRequest.password
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(userSignUpResponse)
    }

    @PostMapping("/auth/user/login")
    fun login(
        @RequestBody userLogInRequest: UserLogInRequest,
        request: HttpServletRequest
    ): ResponseEntity<UserLogInResponse> {
        val userLogInResponse = userService.login(
            nickname = userLogInRequest.nickname,
            password = userLogInRequest.password,
            ip = request.remoteAddr
        )
        return ResponseEntity.ok(userLogInResponse)
    }

    @PostMapping("/user/logout")
    fun logout(
        @RequestHeader(name = "Authorization") authHeader: String
    ): ResponseEntity<Void> {
        // 토큰 추출
        val token = authHeader
            .takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
            ?: return ResponseEntity.noContent().build()

        // 이미 만료된 토큰이면, 에러 처리 안하고 그냥 pass
        if(!jwtTokenProvider.tokenValidateCheck(token)) return ResponseEntity.noContent().build()

        userService.logout(token)
        return ResponseEntity.ok().build()
    }
}
