package com.project.rainmind.weather.controller

import com.project.rainmind.weather.dto.DayWeatherForecastResponse
import com.project.rainmind.weather.service.WeatherForecastService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime

@RestController
class WeatherForecastController(
    private val weatherForecastService: WeatherForecastService,
) {
    // region name + date
    // 요청한 날짜, 요청한 시각의 날씨를 가져온다
    @GetMapping("/v1/weather/forecast")
    fun getDayWeatherForecast(
        @RequestParam regionName: String,
        @RequestParam date: String,
        @RequestParam time: String
    ): DayWeatherForecastResponse {
        val date_convert = LocalDate.parse(date)
        val time_convert = LocalTime.parse(time)
        return weatherForecastService.getDayForecast(regionName, date_convert, time_convert)
    }
}