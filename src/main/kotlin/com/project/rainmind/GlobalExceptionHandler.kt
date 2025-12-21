package com.project.rainmind

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
// JSON key - value
class GlobalExceptionHandler {
    @ExceptionHandler(GlobalException::class)
    fun handle(
        e: GlobalException,
    ): ResponseEntity.s
}