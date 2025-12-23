package com.project.rainmind.weather.weatherfetch.controller

import com.project.rainmind.weather.weatherfetch.dto.DayWeatherForecastResponse
import com.project.rainmind.weather.weatherfetch.service.WeatherGetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class WeatherGetController(
    @Autowired
    private val weatherGetService: WeatherGetService,
) {
    // region name + date
    @GetMapping("/v1/weather/forecast")
    fun getDayWeatherForecast(
        @RequestParam regionName: String,
        @RequestParam date: String
    ): DayWeatherForecastResponse {
        val date_convert = LocalDate.parse(date)
        return weatherGetService.getDayForecast(regionName, date_convert)
    }
}