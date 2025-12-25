package com.project.rainmind.user.controller

import com.project.rainmind.user.service.UserLogOutService
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

    ): ResponseEntity<Void> {
        userLogOutService.logout()
        return ResponseEntity.ok().build()
    }
}