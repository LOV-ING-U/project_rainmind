package com.project.rainmind.user.login.service

import com.project.rainmind.user.NonExistingUsernameException
import com.project.rainmind.user.PasswordNotCorrectException
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.jwt.JwtTokenProvider
import com.project.rainmind.user.login.repository.UserLogInRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class UserLogInService(
    @Autowired
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