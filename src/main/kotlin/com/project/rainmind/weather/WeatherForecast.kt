package com.project.rainmind.weather

import java.time.LocalDateTime

// 격자(nx, ny)의 한 예보 시각 날씨(기상청 단기예보 코드 기준)
data class WeatherForecast(
    val nx: Int,
    val ny: Int,
    val fcstAt: LocalDateTime,
    val sky: Int?,    // 하늘상태 1 맑음, 3 구름많음, 4 흐림
    val pty: Int?,    // 강수형태 0 없음, 1 비, 2 비/눈, 3 눈, 4 소나기
    val pop: Int?,    // 강수확률(%)
    val tmp: Double?  // 기온(℃)
) {
    // 알림 메시지용 요약
    fun describe(): String {
        val parts = mutableListOf<String>()
        val rain = when (pty) { 1 -> "비"; 2 -> "비/눈"; 3 -> "눈"; 4 -> "소나기"; else -> null }
        val skyText = when (sky) { 1 -> "맑음"; 3 -> "구름많음"; 4 -> "흐림"; else -> null }
        (rain ?: skyText)?.let { parts += it }
        pop?.let { parts += "강수확률 $it%" }
        tmp?.let { parts += "기온 ${it}℃" }
        return if (parts.isEmpty()) "(정보 없음)" else parts.joinToString(", ")
    }
}
