package com.project.rainmind.jwt

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${security.jwt.secret}")
    private val secretKey: String,
    @Value("\${security.jwt.access-token-exp-ms}")
    private val accessTokenExpMs: Long
) {
    // reusable key with hmac sha
    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun createToken(
        nickname: String
    ): String {
        val now = Date()
        val valid_period = Date(now.time + accessTokenExpMs)

        // payload 에 순서대로 nickname, 발급시각, 만료시각 입력 후 signature(with HS256) 후 compact(이어붙임)
        return Jwts.builder().setSubject(nickname).setIssuedAt(now).setExpiration(valid_period).signWith(key, SignatureAlgorithm.HS256).compact()
    }

    fun tokenValidateCheck(
        token: String
    ): Boolean {
        return try {
            // verify with key(signature 검증을 이걸로 한다)
            // -> build() : 실제 parser 객체 build
            // -> parseSignedClaims : token을 3부분(header/payload/signature)으로 커팅
            // 이후 signature 부분으로 실제 검증(시간 만료 여부도 검증 -> 27줄에 jwts builder 에 set expiration 넣었기 때문)
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getNickNameFromToken(
        token: String
    ): String {
        // 위의 createToken 함수에서 nickname을 set subject 에 넣음
        val payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        return payload.subject
    }

    fun getRemainTimeOfTokenMS(
        token: String
    ): Long {
        val time = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.expiration.time
        val remain = time - System.currentTimeMillis()

        if(remain < 0) return 0
        else return remain

    }
}