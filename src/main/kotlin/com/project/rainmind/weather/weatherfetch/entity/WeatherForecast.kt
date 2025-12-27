package com.project.rainmind.weather.weatherfetch.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "weather_forecast")
class WeatherForecast (
    @Id var id: Long? = null,
    @Column("region_code") var regionCode: Long,
    @Column("pop") var pop: Int,
    @Column("pty") var pty: Int,
    @Column("pcp") var pcp: String,
    @Column("sky") var sky: Int,
    @Column("wsd") var wsd: Double,
    @Column("base_date_and_time") var baseDateAndTime: LocalDateTime,
    @Column("fcst_date_and_time") var fcstDateAndTime: LocalDateTime,
    @Column("fetched_at") var fetchedAt: LocalDateTime
)