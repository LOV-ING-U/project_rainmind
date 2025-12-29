package com.project.rainmind.weather.weatherfetch

import com.project.rainmind.weather.weatherfetch.dto.KmaWeatherFutureFetchResponse
import com.project.rainmind.weather.weatherfetch.dto.KmaWeatherNowFetchResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.beans.factory.annotation.Value

@Component
class KmaExternalFetchClient (
    private val weatherNowFetchClient: RestClient,
    @Value("\${kma.auth-key}") private val serviceKey: String
) {
    // 초단기실황조회
    fun fetchNowWeather(
        baseDate: String,
        baseTime: String,
        nx: Int,
        ny: Int
    ): KmaWeatherNowFetchResponse {
        // get 요청을 만든다(외부 api로 보낼)
        return weatherNowFetchClient.get()
            .uri { builder ->
                builder
                    .path("/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", nx)
                    .queryParam("ny", ny).build()
            }.retrieve() // retrieve : 실제 요청 보내고, 아래에서 JSON 응답 역직렬화해서 DTO로 받는다.
            .body(KmaWeatherNowFetchResponse::class.java)
            ?: throw ExternalWeatherFetchErrorException()
    }

    // 단기예보조회
    fun fetchFutureWeather(
        baseDate: String,
        baseTime: String,
        nx: Int,
        ny: Int
    ): KmaWeatherFutureFetchResponse {
        return weatherNowFetchClient.get()
            .uri { builder ->
                builder
                    .path("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 2000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", nx)
                    .queryParam("ny", ny).build()
            }.retrieve()
            .body(KmaWeatherFutureFetchResponse::class.java)
            ?: throw ExternalWeatherFetchErrorException()
    }
}