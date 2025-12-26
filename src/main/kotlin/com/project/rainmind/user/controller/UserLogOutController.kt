package com.project.rainmind.user.controller

import com.project.rainmind.user.jwt.JwtTokenProvider
import com.project.rainmind.user.service.UserLogOutService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserLogOutController (
    @Autowired
    private val userLogOutService: UserLogOutService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/v1/user/logout")
    fun logout(
        @RequestHeader(name = "Authorization") authHeader: String
    ): ResponseEntity<Void> {
        // http servlet request 헤더만 받는다.
        // 토큰 추출
        val token = authHeader
            .takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")
            ?: return ResponseEntity.noContent().build()

        // 이미 만료된 토큰이면, 에러 처리 안하고 그냥 pass
        if(!jwtTokenProvider.tokenValidateCheck(token)) return ResponseEntity.noContent().build()

        userLogOutService.logout(token)
        return ResponseEntity.ok().build()
    }
}