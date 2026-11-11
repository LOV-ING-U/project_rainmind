package com.project.rainmind.region

import com.project.rainmind.exception.GlobalException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class RegionException(
    http_errCode: HttpStatusCode,
    http_errCode_cause: Int,
    errMessage: String,
    cause: Throwable? = null
): GlobalException(http_errCode, http_errCode_cause, errMessage, cause)

class InvalidRegionCodeException :
    RegionException(
        http_errCode = HttpStatus.BAD_REQUEST,
        http_errCode_cause = 3,
        errMessage = "Invalid region code. Try again."
    )
