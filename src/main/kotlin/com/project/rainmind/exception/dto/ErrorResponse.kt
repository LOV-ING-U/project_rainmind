package com.project.rainmind.exception.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 응답 : 예외 처리시 해당 객체를 응답으로 전달")
data class ErrorResponse (
    @Schema(description = "에러 메시지 출력")
    val errMessage: String,
    @Schema(description = "서버 내부 에러코드")
    val err_code_internal: Int
)