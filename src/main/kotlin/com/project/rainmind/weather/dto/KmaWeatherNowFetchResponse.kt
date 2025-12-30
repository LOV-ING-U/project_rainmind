package com.project.rainmind.weather.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "실제 kma 서버 초단기실황조회 응답")
data class KmaWeatherNowFetchResponse (
    val response: TotalResponseNow
) {
    @Schema(description = "응답 전체 구조")
    data class TotalResponseNow(
        val header: HeaderNow,
        val body: BodyNow
    )

    @Schema(description = "header 구성요소", example = "\"resultCode\":\"00\", \"resultMsg\":\"NORMAL_SERVICE\"")
    data class HeaderNow(
        val resultCode: String,
        val resultMsg: String
    )

    @Schema(description = "body 구성요소", example = "\"dataType\":\"JSON\", \"items\": {\"item\":{...")
    data class BodyNow(
        val dataType: String,
        val items: ItemsNow
    )

    @Schema(description = "item 리스트")
    data class ItemsNow(
        val item: List<ItemNow>
    )

    @Schema(description = "Item 구성요소", example = "\"baseDate\":\"20251226\", \"baseTime\":\"0600\", \"category\":\"PTY\", \"nx\":\"55\", \"ny\":\"127\", \"obsrValue\":\"0\"")
    data class ItemNow(
        val baseDate: String,
        val baseTime: String,
        val category: String,
        val obsrValue: String
    )
}