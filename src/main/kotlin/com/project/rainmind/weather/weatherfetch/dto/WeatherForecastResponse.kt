package com.project.rainmind.weather.weatherfetch.dto

import com.project.rainmind.weather.weatherfetch.entity.WeatherForecast
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

// response list에 entity 바로 담지 않고, 별도 dto 분리
@Schema(description = "단기예보조회 응답")
data class DayWeatherForecastResponse(
    val regionName: String,
    val date: String,
    val response: List<WeatherForecastItem>
)

@Schema(description = "response list 응답요소 형식")
data class WeatherForecastItem(
    val pop: Int,
    val pty: Int,
    val pcp: String,
    val sky: Int,
    val wsd: Double,
    val fcst_date_and_time: LocalDateTime
)