package com.project.rainmind.user.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class UserLogOutService (
    private val stringRedisTemplate: StringRedisTemplate
) {
    fun logout(
        token: String
    ) {
        // opsForValue => string 연산(redis 에서) 처리하는 interface 반환받음
        stringRedisTemplate.opsForValue().set("blacklist:$token", "expired")
    }
}