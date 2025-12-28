package com.project.rainmind.weather.weatherfetch.service

import com.project.rainmind.weather.weatherfetch.ExternalWeatherFetchErrorException
import com.project.rainmind.weather.weatherfetch.KmaExternalFetchClient
import com.project.rainmind.weather.weatherfetch.InvalidRegionNameException
import com.project.rainmind.weather.weatherfetch.dto.WeatherFutureFetchResponse
import com.project.rainmind.weather.weatherfetch.dto.WeatherNowFetchResponse
import com.project.rainmind.weather.weatherfetch.entity.WeatherForecast
import com.project.rainmind.weather.weatherfetch.repository.LocationRepository
import com.project.rainmind.weather.weatherfetch.repository.WeatherForecastRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class WeatherFetchService (
    private val kmaExternalFetchClient: KmaExternalFetchClient,
    private val locationRepository: LocationRepository,
    private val weatherForecastRepository: WeatherForecastRepository
) {
    private val fetchTimes = listOf("0200", "0500", "0800", "1100", "1400", "1700", "2000", "2300")

    // yyyymmdd, hhmm 형식
    fun getCurrentWeather(
        baseDate: String,
        baseTime: String,
        regionName: String
    ): WeatherNowFetchResponse {
        // api 호출에는 : baseDate/Time + nx/ny 가 필요
        // regionName 으로 nx, ny를 찾는다.
        val location = locationRepository.findByRegionName(regionName) ?: throw InvalidRegionNameException()

        // 주입한 external client 가 api 호출
        val response = kmaExternalFetchClient.fetchNowWeather(baseDate, baseTime, location.nx, location.ny)

        // response => KmaWeatherNowFetchResponse 타입
        // 이를 우리 DTO = WeatherNowFetchResponse 로 바꾼다.
        // 그렇다면, KmaWeatherNowFetchResponse 의 items = list<item> 에서 추출하면 됨
        val items = response.response.body.items // List<Item>

        val rn1Response = items.firstOrNull {
            it.category == "RN1"
        }?.obsrValue

        val ptyResponse = items.firstOrNull {
            it.category == "PTY"
        }?.obsrValue

        val wsdResponse = items.firstOrNull {
            it.category == "WSD"
        }?.obsrValue

        if(rn1Response == null || ptyResponse == null || wsdResponse == null) throw ExternalWeatherFetchErrorException()

        return WeatherNowFetchResponse(
            regionName = regionName,
            baseDate = baseDate,
            baseTime = baseTime,
            rn1 = rn1Response,
            pty = ptyResponse,
            wsd = wsdResponse
        )
    }

    // 외부 api를 호출해서 최신 날씨 예보 정보를 받아오고,
    // DB에 해당 최신정보를 저장한다.
    // 과거의 정보는 모두 삭제. 응답은 저장한 갯수를 return 한다.
    // 삭제 + 삽입 원자적으로 => transactional
    @Transactional
    fun getFutureWeather(
        regionName: String
    ): WeatherFutureFetchResponse {
        val timeNow = LocalDateTime.now()
        val location = locationRepository.findByRegionName(regionName) ?: throw InvalidRegionNameException()

        val (baseDate, baseTime) = calculateBaseDateTime(timeNow)
        val response = kmaExternalFetchClient.fetchFutureWeather(baseDate, baseTime, location.nx, location.ny)

        // DB에 저장 과정
        // 1. 응답 중 오늘 날씨만 남김
        // List<ItemFuture>
        val items = response.response.body.items

        // Map<Pair<String, String>, List<ItemFuture>>
        // ex : "20251226", "0500" -> 12개 줄의 응답(시간대 1개 당 12개 줄)
        val itemsGroup = items.groupBy {
            it.fcstDate to it.fcstTime
        }

        // 2. 기존 DB 전부 삭제
        weatherForecastRepository.deleteAllByRegionCode(location.id!!)

        // 3. 새 데이터로 갈아끼우기
        val baseDateTime = LocalDateTime.parse(baseDate + baseTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
        val newForecastData = itemsGroup.mapNotNull { (pair, list) ->
            // pair 와 그 원소인 list에 대해,
            val fcstDate = pair.first
            val fcstTime = pair.second
            val fcstDateTime = LocalDateTime.parse(fcstDate + fcstTime, DateTimeFormatter.ofPattern("yyyyMMddHHmm"))

            // 오늘꺼 & 지금 이후꺼만 남기기
            if(fcstDateTime.toLocalDate() != timeNow.toLocalDate() || fcstDateTime.toLocalTime() < timeNow.toLocalTime()) return@mapNotNull null
            else {
                // 유효한 데이터(= list)들은 12개 줄의 응답을 가지고 있다(카테고리가 12개)
                // 각 1개의 카테고리마다 map의 원소로 저장
                val listCategoryMap = list.associate {
                    it.category to it.fcstValue
                }

                WeatherForecast(
                    regionCode = location.id!!,
                    pop = listCategoryMap["POP"]?.toIntOrNull() ?: 0,
                    pty = listCategoryMap["PTY"]?.toIntOrNull() ?: 0,
                    pcp = listCategoryMap["PCP"] ?: "0",
                    sky = listCategoryMap["SKY"]?.toIntOrNull() ?: 0,
                    wsd = listCategoryMap["WSD"]?.toDoubleOrNull() ?: 0.0,
                    baseDateAndTime = baseDateTime,
                    fcstDateAndTime = fcstDateTime,
                    fetchedAt = timeNow
                )
            }
        }

        weatherForecastRepository.saveAll(newForecastData)
        return WeatherFutureFetchResponse(
            storeCount = newForecastData.size
        )
    }

    // 기상청 api 는 시각(ex: 2시 => 0200)을 정확히 입력해야 한다.
    // 따라서 날것의 time 을 보정 필요
    // ex: 0200 이전 -> 전날 23:00 사용,
    // 0200 ~ 0500 -> 0200 사용,....
    // 23:00 ~ 23:59:59 -> 23:00 사용
    private fun calculateBaseDateTime(
        time: LocalDateTime
    ): Pair<String, String> {
        // date, time pair
        // 시간을 떼어낸 후, fetchTimes 를 time 으로 만든 후에 떼어낸 시각 이하인것 중 가장 큰 값을 찾음
        val nowTime = time.toLocalTime()
        val biggestTime = fetchTimes
            .map {
                LocalTime.parse(it, DateTimeFormatter.ofPattern("HHmm"))
            }
            .filter { it <= nowTime }
            .maxOrNull()

        if(biggestTime == null) {
            // 즉 0200 이전인 경우
            val baseDate = time.toLocalDate().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            return baseDate to "2300"
        } else {
            // 당일인 경우
            val baseDate = time.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            return baseDate to biggestTime.format(DateTimeFormatter.ofPattern("HHmm"))
        }
    }
}