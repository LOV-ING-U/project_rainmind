package com.project.rainmind.weather.service

import com.project.rainmind.weather.InvalidRegionNameException
import com.project.rainmind.weather.dto.DayWeatherForecastResponse
import com.project.rainmind.weather.dto.WeatherForecastItem
import com.project.rainmind.weather.repository.LocationRepository
import com.project.rainmind.weather.repository.WeatherForecastRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime

@Service
class WeatherForecastService(
    private val weatherForecastRepository: WeatherForecastRepository,
    private val locationRepository: LocationRepository,
) {
    fun getDayForecast(
        regionName: String,
        date: LocalDate,
        time: LocalTime
    ): DayWeatherForecastResponse {
        // parameter of weatherGetRepository
        // 1. regionCode: Int
        // 2. start: LocalDateTime
        // 3. end: LocalDateTime

        // 1. location
        val location = locationRepository.findByRegionName(regionName) ?: throw InvalidRegionNameException()

        // 2 & 3 : start and end
        val targetDateTime = date.atTime(time)
        val weatherForecast = weatherForecastRepository.findOneByRegionCodeAndTargetTime(location.id!!, targetDateTime)

        val response = if(weatherForecast == null){
            emptyList()
        } else {
            listOf(
                WeatherForecastItem(
                    pop = weatherForecast.pop,
                    pty = weatherForecast.pty,
                    pcp = weatherForecast.pcp,
                    sky = weatherForecast.sky,
                    wsd = weatherForecast.wsd,
                    fcst_date_and_time = weatherForecast.fcstDateAndTime
                )
            )
        }

        return DayWeatherForecastResponse(
            regionName = regionName,
            date = date.toString(),
            response = response
        )
    }
}