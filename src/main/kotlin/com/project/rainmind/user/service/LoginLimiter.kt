package com.project.rainmind.user.service

import com.project.rainmind.user.TooManyLoginAttemptsException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LoginLimiter(
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val countTtl = Duration.ofSeconds(60)
    private val blockTtl = Duration.ofSeconds(180)
    private val limit = 10000L

    fun throwIfBlocked(
        ip: String
    ) {
        if (stringRedisTemplate.hasKey(blockKey(ip))) throw TooManyLoginAttemptsException()
    }

    fun onFail(
        ip: String
    ) {
        val key = failKey(ip)
        val cnt = stringRedisTemplate.opsForValue().increment(key) ?: 0L // increment 반환형이 Long...

        if (cnt == 1L) stringRedisTemplate.expire(key, countTtl)

        if (cnt > limit) {
            stringRedisTemplate.opsForValue().set(blockKey(ip), "1", blockTtl)
        }
    }

    fun onSuccess(
        ip: String
    ) {
        stringRedisTemplate.delete(failKey(ip))
    }

    private fun failKey(
        ip: String
    ): String = "login:fail:ip:$ip"

    private fun blockKey(
        ip: String
    ): String = "login:block:ip:$ip"
}

