package com.project.rainmind.schedule

import com.project.rainmind.exception.GlobalException
import org.springframework.http.HttpStatusCode

sealed class ScheduleException (
    http_errCode: HttpStatusCode,
    http_errCode_cause: Int,
    errMessage: String,
    cause: Throwable? = null,
): GlobalException(http_errCode, http_errCode_cause, errMessage, cause)