package com.project.rainmind.user.service

import com.project.rainmind.user.NonExistingUsernameException
import com.project.rainmind.user.PasswordNotCorrectException
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.jwt.JwtTokenProvider
import com.project.rainmind.user.repository.UserLogInRepository
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class UserLogInService(
    private val userLogInRepository: UserLogInRepository,
    private val jwtTokenProvider: JwtTokenProvider
){
    fun login(
        nickname: String,
        password: String
    ): UserLogInResponse {
        val user = userLogInRepository.findByNickname(nickname) ?: throw NonExistingUsernameException()

        if(!BCrypt.checkpw(password, user.passwordHash)) throw PasswordNotCorrectException()

        val token = jwtTokenProvider.createToken(user.nickname)

        return UserLogInResponse(
            nickname = nickname,
            token = token
        )
    }
}