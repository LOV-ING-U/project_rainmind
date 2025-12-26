package com.project.rainmind.weather.weatherfetch.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "초단기실황조회 api 응답")
data class WeatherNowFetchResponse (
    @Schema(description = "지역 이름")
    val regionName: String,
    @Schema(description = "발표 날짜")
    val baseDate: String,
    @Schema(description = "발표 시각")
    val baseTime: String,
    @Schema(description = "1시간 강수량 (mm)")
    val rn1: String,
    @Schema(description = "강수 형태(0 = 없음, 1 = 비, 2 = 진눈깨비, 3 = 눈, 5 = 빗방울, 6 = 빗방울 눈날림, 7 = 눈날림")
    val pty: String,
    @Schema(description = "풍속 (m/s)")
    val wsd: String
)