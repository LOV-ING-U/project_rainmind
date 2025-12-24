package com.project.rainmind.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 로그인 요청")
data class UserLogInRequest (
    @Schema(description = "아이디")
    val nickname: String,
    @Schema(description = "비밀번호")
    val password: String
)