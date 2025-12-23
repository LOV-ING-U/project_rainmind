package com.project.rainmind.weather.weatherfetch.repository

import com.project.rainmind.weather.weatherfetch.entity.WeatherForecast
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import java.time.LocalDateTime

interface WeatherGetRepository : ListCrudRepository<WeatherForecast, Long> {
    // 해당 시간대 사이, 해당 지역의 날씨예보를 전부 받아온다.
    @Query(
        """
            SELECT *
            FROM weather_forecast w
            WHERE w.region_code = :regionCode
            AND w.fcst_date_and_time >= :start
            AND w.fcst_date_and_time < :end
            ORDER BY w.fcst_date_and_time ASC
        """
    )
    fun findAllByRegionCodeAndStartAndEnd(
        regionCode: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<WeatherForecast>
}