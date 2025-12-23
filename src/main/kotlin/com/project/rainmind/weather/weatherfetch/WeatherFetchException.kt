package com.project.rainmind.weather.weatherfetch

import com.project.rainmind.GlobalException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class WeatherFetchException(
    http_errCode: HttpStatusCode,
    http_errCode_cause: Int,
    errMessage: String,
    cause: Throwable? = null
): GlobalException(http_errCode, http_errCode_cause, errMessage, cause)

class InvalidRegionNameException :
        WeatherFetchException(
            http_errCode = HttpStatus.BAD_REQUEST,
            http_errCode_cause = 3,
            errMessage = "Invalid region name input. Try again."
        )