package com.project.rainmind.user.usersignup.service

import com.project.rainmind.user.dto.UserSignUpResponse
import com.project.rainmind.user.usersignup.InvalidPasswordFormatException
import com.project.rainmind.user.usersignup.InvalidUsernameFormatException
import com.project.rainmind.user.usersignup.UsernameAlreadyExistException
import com.project.rainmind.user.usersignup.entity.User
import com.project.rainmind.user.usersignup.repository.UserSignUpRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class UserSignUpService (
    @Autowired
    private val userSignUpRepository: UserSignUpRepository,
){
    fun register(
        nickname: String,
        password: String,
        region_name: String
    ): UserSignUpResponse {
        // 1. user already exist check
        val user_same_name = userSignUpRepository.findByUsername(nickname) ?: throw UsernameAlreadyExistException()
        
        // 2. invalid password or invalid username
        // 현재는 4글자 미만인 경우에만 invalid 가정
        if(nickname.length < 4) throw InvalidUsernameFormatException()
        if(password.length < 4) throw InvalidPasswordFormatException()

        val encrypt_password = BCrypt.hashpw(password, BCrypt.gensalt())
        userSignUpRepository.save(
            User(
                passwordHash = encrypt_password,
                nickname = nickname,
                location = region_name
            )
        )

        return UserSignUpResponse(
            nickname = nickname,
            location = region_name
        )
    }
}