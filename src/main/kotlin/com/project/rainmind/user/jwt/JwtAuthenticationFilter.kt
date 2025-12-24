package com.project.rainmind.user.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter (
    private val jwtTokenProvider: JwtTokenProvider
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
        if(uri_check_noneed(uri)) filterChain.doFilter(request, response)

        // HTTP request message 에서 Authorization : adfasdasdfas... 찾음(토큰 확인)
        val auth_header_token = request.getHeader("Authorization")
        val bearer_token = auth_header_token ?. takeIf {
            it.startsWith("Bearer ")
        } ?. removePrefix("Bearer ")

        // bearer token check
        if(bearer_token != null && jwtTokenProvider.tokenValidateCheck(bearer_token)) {
            filterChain.doFilter(request, response)
        } else response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is empty, or invalid")
    }

    private fun uri_check_noneed(
        uri: String
    ): Boolean = uri.substring(0, 10).equals("/v1/auth/")
}