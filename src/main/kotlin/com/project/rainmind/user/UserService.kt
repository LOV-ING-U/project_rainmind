package com.project.rainmind.user

import com.project.rainmind.jwt.JwtTokenProvider
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.dto.UserSignUpResponse
import com.project.rainmind.user.entity.User
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UserService (
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginLimiter: LoginLimiter,
    private val stringRedisTemplate: StringRedisTemplate
) {
    fun register(
        nickname: String,
        password: String
    ): UserSignUpResponse {
        // 현재는 4글자 미만인 경우에만 invalid 가정
        if(nickname.length < 4) throw InvalidUsernameFormatException()
        if(password.length < 4) throw InvalidPasswordFormatException()

        val encrypt_password = BCrypt.hashpw(password, BCrypt.gensalt(4))

        // 중복 닉네임은 DB unique key 로 최종 방어
        try {
            userRepository.save(
                User(
                    passwordHash = encrypt_password,
                    nickname = nickname
                )
            )
        } catch (e: DataIntegrityViolationException) {
            throw UsernameAlreadyExistException()
        }

        return UserSignUpResponse(
            nickname = nickname
        )
    }

    fun login(
        nickname: String,
        password: String,
        ip: String
    ): UserLogInResponse {
        loginLimiter.throwIfBlocked(ip)

        val user = userRepository.findByNickname(nickname)
        if(user == null) {
            loginLimiter.onFail(ip)
            throw NonExistingUsernameException()
        }

        if(!BCrypt.checkpw(password, user.passwordHash)) {
            loginLimiter.onFail(ip)
            throw PasswordNotCorrectException()
        }

        val token = jwtTokenProvider.createToken(user.nickname)

        return UserLogInResponse(
            nickname = nickname,
            token = token
        )
    }

    // 인증된 토큰을 남은 유효시간만큼 redis 블랙리스트에 등록(TTL)
    fun logout(
        token: String
    ) {
        val ttl_ms = jwtTokenProvider.getRemainTimeOfTokenMS(token)
        stringRedisTemplate.opsForValue().set("jwt:blacklist:$token", "notUsed", ttl_ms, TimeUnit.MILLISECONDS)
    }
}
