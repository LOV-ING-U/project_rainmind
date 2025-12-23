package com.project.rainmind.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

// 어디에서나 상속될 수 있는,
// sealed 선언: 모든 예외 subclass들을 1개의 파일에 선언해야 하므로
// 모든 예외의 모체가 되는 exception은 open으로
open class GlobalException(
    // 1. http status code + 세분화된 error code 위한 field
    val http_errCode: HttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
    val http_errCode_cause: Int,
    // 2. error message
    val errMessage: String,
    // 3. original error cause
    cause: Throwable? = null,
): RuntimeException(errMessage, cause) {
    @Override
    override fun toString(): String {
        return "Global Exception :\nhttpStatus = $http_errCode,\ndetailed error cause = $http_errCode_cause,\nerrMessage = $errMessage\n"
    }
}