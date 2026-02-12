package com.project.rainmind.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationFilter (
    private val jwtTokenProvider: JwtTokenProvider,
    private val stringRedisTemplate: StringRedisTemplate
) : OncePerRequestFilter() {
    // filter logic
    override fun doFilterInternal(
        // JSON -> tomcat 이 http servlet request/response 생성 -> filter 거침(현재)
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // login, register 기능은 jwt 인증없이 바로 통과
        // 단기예보조회, 초단기실황조회 기능은 로그인된 유저 한해서 수행(통과 x)
        val uri = request.requestURI

        // auth 계열(login, register)은 바로 통과
        if(uri_check_noneed(uri)) {
            filterChain.doFilter(request, response)
            return
        }

        // HTTP request message 에서 Authorization : Bearer adfasdasdfas... 찾음(토큰 확인)
        val auth_header_token = request.getHeader("Authorization")
        val bearer_token = auth_header_token ?. takeIf {
            it.startsWith("Bearer ")
        } ?. removePrefix("Bearer ")

        // bearer token check
        if(!bearer_token.isNullOrBlank() && jwtTokenProvider.tokenValidateCheck(bearer_token)) {
            // 일단 validate token 맞음
            // 인증 시작
            // 가장먼저, redis 토큰 블랙리스트 체크
            val blacklist_key = "jwt:blacklist:$bearer_token"
            if(stringRedisTemplate.hasKey(blacklist_key)){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Already Logout user.")
                return
            }

            val nickname = jwtTokenProvider.getNickNameFromToken(bearer_token)

            // spring 이 제공하는 authentication 객체에 nickname을 넣음
            // principal = 사용자 정체성, credential = 인증수단 같은것. 지금은 이미 인증완료 -> null
            // authority = 역할기반 인증(?) -> 지금은 안함
            val authentication = UsernamePasswordAuthenticationToken(
                nickname,
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )

            // log
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

            // spring security 내부 공간에, security context 에 해당 사용자 요청을 공식 등록
            SecurityContextHolder.getContext().authentication = authentication

            // 이후 filter
            filterChain.doFilter(request, response)
        } else response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is empty, or invalid")
    }

    private fun uri_check_noneed(
        uri: String
    ): Boolean = uri.startsWith("/v1/auth/user/login") or uri.startsWith("/v1/auth/user/register") or uri.startsWith("/swagger-ui/") or uri.startsWith("/v3/api-docs")
}