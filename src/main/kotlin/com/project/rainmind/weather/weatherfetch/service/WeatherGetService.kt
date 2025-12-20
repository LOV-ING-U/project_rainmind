package com.project.rainmind.weatherfetch.service

import com.project.rainmind.weatherfetch.repository.WeatherGetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class WeatherGetService(
    @Autowired
    private val weatherGetRepository: WeatherGetRepository,
) {
}