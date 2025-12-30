package com.project.rainmind.schedule

import com.project.rainmind.exception.GlobalException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class ScheduleException (
    http_errCode: HttpStatusCode,
    http_errCode_cause: Int,
    errMessage: String,
    cause: Throwable? = null,
): GlobalException(http_errCode, http_errCode_cause, errMessage, cause)

class TooManySchedulesException :
        ScheduleException (
            http_errCode = HttpStatus.NOT_ACCEPTABLE,
            http_errCode_cause = 1,
            errMessage = "Too many schedules. Delete other schedules and try again."
        )

class ScheduleNotFoundException :
        ScheduleException (
            http_errCode = HttpStatus.NOT_FOUND,
            http_errCode_cause = 1,
            errMessage = "Requested schedule does not exist. Try again."
        )

class InvalidScheduleStartAndEndTimeException :
        ScheduleException (
            http_errCode = HttpStatus.BAD_REQUEST,
            http_errCode_cause = 1,
            errMessage = "Time relation should be start < end. Try again."
        )