package com.project.rainmind.config

import com.project.rainmind.user.jwt.JwtAuthenticationFilter
import com.project.rainmind.user.jwt.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig (
    @Autowired
    private val jwtTokenProvider: JwtTokenProvider,
    private val stringRedisTemplate: StringRedisTemplate
) {
    @Bean
    fun addFilterChain(
        http: HttpSecurity
    ): SecurityFilterChain {
        // 우리가 만든 필터
        val jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenProvider, stringRedisTemplate)
        http.csrf {
            it.disable() // 세션 기반 인증 아님
        }.sessionManagement { // stateless
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }.authorizeHttpRequests { auth ->
            auth.requestMatchers(
                "/v1/auth/**",
            ).permitAll().anyRequest().authenticated() // requestMatchers 것들은 permit all,
            // 그 외의 (any Request) 요청들은 전부 authenticated
        }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        // 등록(우리 것이 가장 먼저 실행되도록)

        // 실제 security filter chain 객체 만듬
        return http.build()
    }
}