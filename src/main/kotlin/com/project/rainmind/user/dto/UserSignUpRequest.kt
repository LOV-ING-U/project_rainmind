package com.project.rainmind.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 회원가입 요청")
data class UserSignUpRequest (
    @Schema(description = "회원가입 닉네임", required = true)
    val nickname: String,
    @Schema(description = "회원가입 비밀번호", required = true)
    val password: String,
    @Schema(description = "살고 있는 지역 이름", required = true)
    val region_name: String,
)