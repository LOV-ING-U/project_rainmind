package com.project.rainmind.weather.weatherfetch.controller

import com.project.rainmind.weather.weatherfetch.dto.WeatherNowFetchResponse
import com.project.rainmind.weather.weatherfetch.service.WeatherNowFetchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/v1/weather")
class WeatherNowFetchController (
    private val weatherNowFetchService: WeatherNowFetchService
) {
    @GetMapping("/current")
    fun getCurrentWeather(
        @RequestParam baseDate: String,
        @RequestParam baseTime: String,
        @RequestParam regionName: String // nx, ny
    ): WeatherNowFetchResponse {
        return weatherNowFetchService.getCurrentWeather(baseDate, baseTime, regionName)
    }
}