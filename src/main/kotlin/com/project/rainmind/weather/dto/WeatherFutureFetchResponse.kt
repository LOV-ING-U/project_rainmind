package com.project.rainmind.weather.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "예보 정보 서버에 저장하는 api - 3시간마다 자동 호출")
data class WeatherFutureFetchResponse (
    val storeCount: Int
)