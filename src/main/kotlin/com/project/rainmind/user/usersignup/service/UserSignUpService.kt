package com.project.rainmind.user.usersignup.service

import com.project.rainmind.user.dto.UserSignUpResponse
import com.project.rainmind.user.usersignup.repository.UserSignUpRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserSignUpService (
    @Autowired
    private val userSignUpRepository: UserSignUpRepository,
){
    fun register(
        nickname: String,
        password: String,
    ): UserSignUpResponse {

    }
}