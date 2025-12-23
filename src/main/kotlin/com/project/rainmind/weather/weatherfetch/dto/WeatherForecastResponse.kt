package com.project.rainmind.weather.weatherfetch.dto

import com.project.rainmind.weather.weatherfetch.entity.WeatherForecast
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

// response list에 entity 바로 담지 않고, 별도 dto 분리
@Schema(description = "단기예보조회 응답")
data class DayWeatherForecastResponse(
    @Schema(description = "지역")
    val regionName: String,
    @Schema(description = "날짜")
    val date: String,
    @Schema(description = "해당 날짜의 하루동안의 모든 예보 정보(시간대별)")
    val response: List<WeatherForecastItem>
)

@Schema(description = "response list 응답요소 형식")
data class WeatherForecastItem(
    @Schema(description = "강수확률 : %")
    val pop: Int,
    @Schema(description = "강수형태 : 0 = 없음, 1 = 비, 2 = 진눈깨비, 3 = 눈, 4 = 소나기")
    val pty: Int,
    @Schema(description = "1시간 강수량 : mm")
    val pcp: String,
    @Schema(description = "하늘상태 : 1 = 맑음, 3 = 구름많음, 4 = 흐림")
    val sky: Int,
    @Schema(description = "풍속 : m/s")
    val wsd: Double,
    @Schema(description = "예보날짜와 시각")
    val fcst_date_and_time: LocalDateTime
)