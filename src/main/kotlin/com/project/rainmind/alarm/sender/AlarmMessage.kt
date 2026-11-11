package com.project.rainmind.alarm.sender

import com.project.rainmind.weather.WeatherForecast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

data class AlarmMessage(
    val scheduleId: Long,
    val userId: Long,
    val title: String,
    val startAt: LocalDateTime,
    val regionName: String,
    // 날씨는 best-effort. 없으면 null
    val weather: WeatherForecast?
) {
    fun render(): String = """
        |[RainMind] 30분 뒤 일정이 있어요
        |· 일정: $title
        |· 시간: ${startAt.format(TIME_FORMAT)}
        |· 장소: $regionName
        |· 날씨: ${weather?.describe() ?: "(정보 없음)"}
    """.trimMargin()
}
