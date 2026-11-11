package com.project.rainmind.weather

import com.project.rainmind.common.SEOUL
import com.project.rainmind.region.RegionCatalog
import com.project.rainmind.weather.dto.KmaWeatherFutureFetchResponse
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")
private val FCST_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

// 기상청 단기예보를 주기적으로 받아 DB 와 Redis 에 저장한다.
// 테스트용 설정이라 12시간마다 격자별로 순차 호출한다. 실패한 격자는 건너뛰고 이전 값을 유지한다.
@Component
class WeatherCollectWorker(
    private val regionCatalog: RegionCatalog,
    private val weatherStore: WeatherStore,
    @Value("\${kma.auth-key}") private val serviceKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val kmaClient = RestClient.create("https://apis.data.go.kr")

    // 02시, 14시 발표분이 제공된 뒤(20분)에 실행
    @Scheduled(cron = "0 20 2,14 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "weatherCollectLock", lockAtMostFor = "PT2H")
    fun collect() {
        collectOnce(LocalDateTime.now(SEOUL))
    }

    fun collectOnce(now: LocalDateTime): Int {
        // cron 이 02:20, 14:20 에만 실행되므로 실행 시각의 정시가 곧 발표 시각(02:00, 14:00)이다.
        // cron 을 바꾸면 기상청 발표 시각(02, 05, 08, 11, 14, 17, 20, 23시)에 맞춰야 한다.
        val base = now.truncatedTo(ChronoUnit.HOURS)
        var saved = 0

        for ((nx, ny) in regionCatalog.grids()) {
            try {
                val forecasts = toForecasts(nx, ny, fetch(base, nx, ny))
                weatherStore.saveAll(forecasts, baseAt = base, fetchedAt = now)
                saved += forecasts.size
            } catch (e: Exception) {
                log.warn("날씨 수집 실패 (nx={}, ny={}): {}", nx, ny, e.message)
            }
        }

        weatherStore.deleteBefore(now.minusDays(1))
        log.info("날씨 수집 완료: base={}, 저장 {}건", base, saved)
        return saved
    }

    // 기상청 단기예보조회 API 호출
    private fun fetch(base: LocalDateTime, nx: Int, ny: Int): List<KmaWeatherFutureFetchResponse.ItemFuture> =
        kmaClient.get()
            .uri { builder ->
                builder
                    .path("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 2000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", base.format(DATE_FORMAT))
                    .queryParam("base_time", base.format(TIME_FORMAT))
                    .queryParam("nx", nx)
                    .queryParam("ny", ny)
                    .build()
            }
            .retrieve()
            .body(KmaWeatherFutureFetchResponse::class.java)
            ?.response?.body?.items?.item
            ?: error("기상청 응답이 비어 있음")

    // 기상청 응답은 "예보시각 x 항목" 마다 한 줄이다(15시 SKY, 15시 PTY, 15시 POP, 15시 TMP, 15시 REH ...).
    // 이를 "예보시각" 마다 하나(15시: sky, pty, pop, tmp)로 바꾼다.
    private fun toForecasts(
        nx: Int,
        ny: Int,
        items: List<KmaWeatherFutureFetchResponse.ItemFuture>
    ): List<WeatherForecast> {
        // 1. 예보 시각별로 묶는다: "202611121500" -> [SKY 줄, PTY 줄, POP 줄, TMP 줄, ...]
        val itemsByTime = items.groupBy { it.fcstDate + it.fcstTime }

        val forecasts = mutableListOf<WeatherForecast>()
        for ((time, itemsAtTime) in itemsByTime) {
            // 2. 그 시각의 줄들을 "항목 -> 값" 으로: {SKY=1, PTY=0, POP=20, TMP=3.5, REH=60, ...}
            val valueOf = itemsAtTime.associate { it.category to it.fcstValue }

            // 3. 필요한 4개 항목만 꺼내 하나로 만든다
            forecasts += WeatherForecast(
                nx = nx,
                ny = ny,
                fcstAt = LocalDateTime.parse(time, FCST_FORMAT),
                sky = valueOf["SKY"]?.toIntOrNull(),
                pty = valueOf["PTY"]?.toIntOrNull(),
                pop = valueOf["POP"]?.toIntOrNull(),
                tmp = valueOf["TMP"]?.toDoubleOrNull()
            )
        }
        return forecasts
    }
}
