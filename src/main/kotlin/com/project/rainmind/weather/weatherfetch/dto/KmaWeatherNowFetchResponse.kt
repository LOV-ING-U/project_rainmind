package com.project.rainmind.weather.weatherfetch.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "실제 kma server 응답")
data class KmaWeatherNowFetchResponse (
    val totalResponse: TotalResponse
) {
    @Schema(description = "응답 전체 구조")
    data class TotalResponse(
        val header: Header,
        val body: Body
    )

    @Schema(description = "header 구성요소", example = "\"resultCode\":\"00\", \"resultMsg\":\"NORMAL_SERVICE\"")
    data class Header(
        val resultCode: String,
        val resultMsg: String
    )

    @Schema(description = "body 구성요소", example = "\"dataType\":\"JSON\", \"items\": {\"item\":{...")
    data class Body(
        val dataType: String,
        val items: List<Item>
    )

    @Schema(description = "Item 구성요소", example = "\"baseDate\":\"20251226\", \"baseTime\":\"0600\", \"category\":\"PTY\", \"nx\":\"55\", \"ny\":\"127\", \"obsrValue\":\"0\"")
    data class Item(
        val baseDate: String,
        val baseTime: String,
        val category: String,
        val obsrValue: String
    )
}