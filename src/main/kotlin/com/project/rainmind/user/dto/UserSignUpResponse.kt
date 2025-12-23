package com.project.rainmind.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 회원가입 요청의 응답")
data class UserSignUpResponse(
    @Schema(description = "회원가입 닉네임", required = true)
    val nickname: String,
    @Schema(description = "회원가입 시 기입한 지역 정보", required = true)
    val location: String,
)