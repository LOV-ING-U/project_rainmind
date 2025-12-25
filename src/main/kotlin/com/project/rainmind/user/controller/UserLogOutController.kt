package com.project.rainmind.user.controller

import com.project.rainmind.user.service.UserLogOutService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserLogOutController (
    @Autowired
    private val userLogOutService: UserLogOutService
) {
    @PostMapping("/v1/user/logout")
    fun logout(
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val tokenFromRequest = request.getAttribute("token") as String
        userLogOutService.logout(tokenFromRequest)
        return ResponseEntity.ok().build()
    }
}