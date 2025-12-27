package com.project.rainmind.weather.weatherfetch.controller

import com.project.rainmind.weather.weatherfetch.dto.DayWeatherForecastResponse
import com.project.rainmind.weather.weatherfetch.service.WeatherForecastService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class WeatherForecastController(
    private val weatherForecastService: WeatherForecastService,
) {
    // region name + date
    // 요청한 날짜의 날씨정보를 모두 가져오는 api
    @GetMapping("/v1/weather/forecast")
    fun getDayWeatherForecast(
        @RequestParam regionName: String,
        @RequestParam date: String
    ): DayWeatherForecastResponse {
        val date_convert = LocalDate.parse(date)
        return weatherForecastService.getDayForecast(regionName, date_convert)
    }
}