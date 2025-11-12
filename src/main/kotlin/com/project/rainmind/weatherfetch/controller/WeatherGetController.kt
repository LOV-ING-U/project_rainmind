package com.project.rainmind.weatherfetch.controller

import com.project.rainmind.weatherfetch.service.WeatherGetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WeatherGetController(
    @Autowired
    private val weatherGetService: WeatherGetService,
) {
    @GetMapping("/v1/weather")
    fun getWeather(

    ): {

    }
}