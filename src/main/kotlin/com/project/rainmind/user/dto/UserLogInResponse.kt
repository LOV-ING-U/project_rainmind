package com.project.rainmind.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 로그인 응답")
data class UserLogInResponse (
    @Schema(description = "유저 이름")
    val nickname: String,
    @Schema(description = "토큰")
    val token: String
)