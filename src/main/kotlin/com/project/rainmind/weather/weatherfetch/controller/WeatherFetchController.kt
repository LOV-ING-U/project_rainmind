package com.project.rainmind.weather.weatherfetch.controller

import com.project.rainmind.weather.weatherfetch.dto.WeatherFutureFetchResponse
import com.project.rainmind.weather.weatherfetch.dto.WeatherNowFetchResponse
import com.project.rainmind.weather.weatherfetch.service.WeatherFetchService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/v1/weather")
class WeatherFetchController (
    private val weatherFetchService: WeatherFetchService
) {
    @GetMapping("/current")
    fun getCurrentWeather(
        @RequestParam baseDate: String,
        @RequestParam baseTime: String,
        @RequestParam regionName: String // nx, ny
    ): WeatherNowFetchResponse {
        return weatherFetchService.getCurrentWeather(baseDate, baseTime, regionName)
    }

    // @Scheduled(cron = "0 0 2,5,8,11,14,17,20,23 * * *", zone = "Asia/Seoul")
    @GetMapping("/today")
    fun getFutureWeather(
        @RequestParam regionName: String
    ): WeatherFutureFetchResponse {
        return weatherFetchService.getFutureWeather(regionName)
    }
}