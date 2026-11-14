package com.project.rainmind.unit

import com.project.rainmind.weather.dto.KmaWeatherFutureFetchResponse.ItemFuture
import com.project.rainmind.weather.toForecasts
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

// 단위 테스트: 기상청 응답(예보시각 x 항목 한 줄씩)을 예보시각 단위로 묶는 변환
class KmaForecastParsingTest {

    private fun item(time: String, category: String, value: String) = ItemFuture(
        baseDate = "20261114", baseTime = "1400", category = category,
        fcstDate = "20261114", fcstTime = time, fcstValue = value, nx = 59, ny = 125
    )

    @Test
    fun `예보시각별로 묶어 필요한 4개 항목만 꺼낸다`() {
        val items = listOf(
            item("1500", "SKY", "1"), item("1500", "PTY", "0"), item("1500", "POP", "20"),
            item("1500", "TMP", "3.5"), item("1500", "REH", "60"),
            item("1600", "SKY", "4"), item("1600", "PTY", "1"), item("1600", "POP", "70"), item("1600", "TMP", "2")
        )

        val result = toForecasts(59, 125, items).associateBy { it.fcstAt }

        assertEquals(2, result.size)
        val at15 = result.getValue(LocalDateTime.of(2026, 11, 14, 15, 0))
        assertEquals(1, at15.sky)
        assertEquals(0, at15.pty)
        assertEquals(20, at15.pop)
        assertEquals(3.5, at15.tmp)

        val at16 = result.getValue(LocalDateTime.of(2026, 11, 14, 16, 0))
        assertEquals(1, at16.pty)
        assertEquals(2.0, at16.tmp)
    }

    @Test
    fun `항목이 빠졌거나 숫자가 아니면 null 로 둔다`() {
        val items = listOf(item("1500", "SKY", "1"), item("1500", "POP", "-"))

        val forecast = toForecasts(59, 125, items).single()

        assertEquals(1, forecast.sky)
        assertNull(forecast.pty)
        assertNull(forecast.pop)
        assertNull(forecast.tmp)
    }
}
