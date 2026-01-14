package com.project.rainmind.exception

import com.project.rainmind.exception.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
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

    // @Valid 실패 시, 실행
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        ErrorResponse( // 검증 오류결과 . 필드 오류 리스트. 첫번째 에러 ?. 설정된 기본 메시지 ?: 메시지 없으면 기본값
            errMessage = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Invalid request",
            err_code_internal = 11
        )
    )
}
