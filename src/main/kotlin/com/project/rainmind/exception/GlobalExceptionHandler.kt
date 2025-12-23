package com.project.rainmind.exception

import com.project.rainmind.exception.dto.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

// controller 모든 예외를 가로챔
@RestControllerAdvice
// JSON key - value
class GlobalExceptionHandler {
    // 예외 타입이 GlobalException 이면, 이 메서드 실행
    @ExceptionHandler(GlobalException::class)
    fun handle(
        e: GlobalException,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.http_errCode).body(
            ErrorResponse(
                errMessage = e.errMessage,
                err_code_internal = e.http_errCode_cause
            )
        )
}
