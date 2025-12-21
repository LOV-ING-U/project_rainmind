package com.project.rainmind.weather.weatherfetch.controller

import com.project.rainmind.weather.weatherfetch.service.WeatherGetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WeatherGetController(
    @Autowired
    private val weatherGetService: WeatherGetService,
) {
    @GetMapping("/v1/weather")
    fun getWeather(

    ): {

    }
}