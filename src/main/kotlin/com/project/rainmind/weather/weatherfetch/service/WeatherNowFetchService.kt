package com.project.rainmind.weather.weatherfetch.service

import com.project.rainmind.weather.weatherfetch.KmaExternalFetchClient
import com.project.rainmind.weather.weatherfetch.InvalidRegionNameException
import com.project.rainmind.weather.weatherfetch.dto.WeatherNowFetchResponse
import com.project.rainmind.weather.weatherfetch.repository.LocationRepository
import org.springframework.stereotype.Service

@Service
class WeatherNowFetchService (
    private val kmaExternalFetchClient: KmaExternalFetchClient,
    private val locationRepository: LocationRepository
) {
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
        val items = response.totalResponse.body.items // List<Item>

        val rn1Response = items.firstOrNull {
            it.category == "RN1"
        }?.obsrValue

        val ptyResponse = items.firstOrNull {
            it.category == "PTY"
        }?.obsrValue

        val wsdResponse = items.firstOrNull {
            it.category == "WSD"
        }?.obsrValue

        return WeatherNowFetchResponse(
            regionName = regionName,
            baseDate = baseDate,
            baseTime = baseTime,
            rn1 = rn1Response!!,
            pty = ptyResponse!!,
            wsd = wsdResponse!!
        )
    }
}