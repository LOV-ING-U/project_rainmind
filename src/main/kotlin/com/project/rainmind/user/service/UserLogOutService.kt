package com.project.rainmind.user.service

import com.project.rainmind.jwt.JwtTokenProvider
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class UserLogOutService (
    private val stringRedisTemplate: StringRedisTemplate,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun logout(
        token: String
    ) {
        // controller 에서 추출한, 인증된 토큰을 건네받아 인증 & redis 에 등록(TTL)
        // opsForValue => string 연산(redis 에서) 처리하는 interface 반환받음
        // 토큰 자체를 key 로 만들면: 보통 GET key로 조회하므로 바로 조회가능(덮어씜 방지)
        val ttl_ms = jwtTokenProvider.getRemainTimeOfTokenMS(token)
        stringRedisTemplate.opsForValue().set("jwt:blacklist:$token", "notUsed", ttl_ms, TimeUnit.MILLISECONDS)
    }
}