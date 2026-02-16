package com.project.rainmind.user.service

import org.springframework.dao.DataIntegrityViolationException
import com.project.rainmind.user.dto.UserSignUpResponse
import com.project.rainmind.user.InvalidPasswordFormatException
import com.project.rainmind.user.InvalidUsernameFormatException
import com.project.rainmind.user.UsernameAlreadyExistException
import com.project.rainmind.user.entity.User
import com.project.rainmind.user.repository.UserSignUpRepository
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class UserSignUpService (
    private val userSignUpRepository: UserSignUpRepository,
){
    fun register(
        nickname: String,
        password: String,
        region_name: String
    ): UserSignUpResponse {
        // 1. user already exist check
       // if(userSignUpRepository.existsByNickname(nickname)) throw UsernameAlreadyExistException()
        
        // 2. invalid password or invalid username
        // 현재는 4글자 미만인 경우에만 invalid 가정
        if(nickname.length < 4) throw InvalidUsernameFormatException()
        if(password.length < 4) throw InvalidPasswordFormatException()

        val encrypt_password = BCrypt.hashpw(password, BCrypt.gensalt(4))
        
        // DB 마지막 방어(unique key)
        try {
            userSignUpRepository.save(
                User(
                    passwordHash = encrypt_password,
                    nickname = nickname,
                    location = region_name
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw UsernameAlreadyExistException()
        }

        return UserSignUpResponse(
            nickname = nickname,
            location = region_name
        )
    }
}
