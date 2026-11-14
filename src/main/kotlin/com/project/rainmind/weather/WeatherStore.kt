package com.project.rainmind.weather

import com.project.rainmind.common.SEOUL
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val KEY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH")

private const val UPSERT_SQL = """
    INSERT INTO weather_forecast (nx, ny, fcst_at, sky, pty, pop, tmp, base_at, fetched_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE
        sky = VALUES(sky), pty = VALUES(pty), pop = VALUES(pop), tmp = VALUES(tmp),
        base_at = VALUES(base_at), fetched_at = VALUES(fetched_at)
"""

// 날씨 저장소
// DB(weather_forecast)가 원본이고, Redis 는 발송 워커가 빠르게 읽기 위한 사본이다.
// Redis 키: weather:{nx}:{ny}:{yyyyMMddHH}, 값: "sky,pty,pop,tmp", TTL: 예보 시각 + 1시간
@Component
class WeatherStore(
    private val jdbcTemplate: JdbcTemplate,
    private val stringRedisTemplate: StringRedisTemplate
) {
    // DB 와 Redis 에 함께 저장한다. 같은 격자·시각이면 최신 발표분으로 덮어쓴다.
    fun saveAll(
        forecasts: List<WeatherForecast>,
        baseAt: LocalDateTime,
        fetchedAt: LocalDateTime
    ) {
        if (forecasts.isEmpty()) return

        jdbcTemplate.batchUpdate(
            UPSERT_SQL.trimIndent(),
            forecasts.map {
                arrayOf<Any?>(
                    it.nx, it.ny, Timestamp.valueOf(it.fcstAt),
                    it.sky, it.pty, it.pop, it.tmp,
                    Timestamp.valueOf(baseAt), Timestamp.valueOf(fetchedAt)
                )
            }
        )

        val now = LocalDateTime.now(SEOUL)
        for (forecast in forecasts) {
            val ttl = Duration.between(now, forecast.fcstAt.plusHours(1))
            if (ttl.isNegative || ttl.isZero) continue
            val value = listOf(forecast.sky, forecast.pty, forecast.pop, forecast.tmp).joinToString(",") { it?.toString() ?: "" }
            stringRedisTemplate.opsForValue().set(key(forecast.nx, forecast.ny, forecast.fcstAt), value, ttl)
        }
    }

    // 일정 시작 시각이 속한 정시의 예보를 Redis 에서 찾는다. 없으면 null(날씨 없이 발송)
    fun find(nx: Int, ny: Int, at: LocalDateTime): WeatherForecast? {
        val fcstAt = at.truncatedTo(ChronoUnit.HOURS)
        val value = stringRedisTemplate.opsForValue().get(key(nx, ny, fcstAt)) ?: return null
        val cols = value.split(",")
        return WeatherForecast(
            nx = nx,
            ny = ny,
            fcstAt = fcstAt,
            sky = cols[0].toIntOrNull(),
            pty = cols[1].toIntOrNull(),
            pop = cols[2].toIntOrNull(),
            tmp = cols[3].toDoubleOrNull()
        )
    }

    // 지난 예보 정리(DB). Redis 는 TTL 로 자동 삭제된다.
    fun deleteBefore(time: LocalDateTime): Int =
        jdbcTemplate.update("DELETE FROM weather_forecast WHERE fcst_at < ?", Timestamp.valueOf(time))

    private fun key(nx: Int, ny: Int, fcstAt: LocalDateTime) = "weather:$nx:$ny:${fcstAt.format(KEY_TIME_FORMAT)}"
}
