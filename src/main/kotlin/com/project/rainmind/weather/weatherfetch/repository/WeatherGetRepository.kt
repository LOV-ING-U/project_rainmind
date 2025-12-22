package com.project.rainmind.weather.weatherfetch.repository

import com.project.rainmind.weather.weatherfetch.entity.WeatherForecast
import org.springframework.data.repository.ListCrudRepository

interface WeatherGetRepository : ListCrudRepository<WeatherForecast, Long> {
    fun find
}