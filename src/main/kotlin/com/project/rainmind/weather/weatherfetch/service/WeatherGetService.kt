package com.project.rainmind.weather.weatherfetch.service

import com.project.rainmind.weather.weatherfetch.InvalidRegionNameException
import com.project.rainmind.weather.weatherfetch.dto.DayWeatherForecastResponse
import com.project.rainmind.weather.weatherfetch.dto.WeatherForecastItem
import com.project.rainmind.weather.weatherfetch.repository.LocationRepository
import com.project.rainmind.weather.weatherfetch.repository.WeatherGetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class WeatherGetService(
    @Autowired
    private val weatherGetRepository: WeatherGetRepository,
    @Autowired
    private val locationRepository: LocationRepository,
) {
    fun getDayForecast(
        regionName: String,
        date: LocalDate
    ): DayWeatherForecastResponse {
        // parameter of weatherGetRepository
        // 1. regionCode: Int
        // 2. start: LocalDateTime
        // 3. end: LocalDateTime

        // 1. location
        val location = locationRepository.findByRegionName(regionName) ?: throw InvalidRegionNameException()

        // 2 & 3 : start and end
        val start = date.atStartOfDay()
        val end = date.plusDays(1).atStartOfDay()

        val weatherForecastList = weatherGetRepository.findAllByRegionCodeAndStartAndEnd(location.id!!, start, end)

        return DayWeatherForecastResponse(
            regionName = regionName,
            date = date.toString(),
            response = weatherForecastList.map {
                WeatherForecastItem(
                    pop = it.pop,
                    pty = it.pty,
                    pcp = it.pcp,
                    sky = it.sky,
                    wsd = it.wsd,
                    fcst_date_and_time = it.fcstDateAndTime,
                )
            }
        )
    }
}