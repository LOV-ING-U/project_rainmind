package com.project.rainmind.weather.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "실제 kma 서버 단기예보조회 api 응답")
data class KmaWeatherFutureFetchResponse (
    val response: TotalResponseFuture
) {
    @Schema(description = "응답 전체 구조")
    data class TotalResponseFuture(
        val header: HeaderFuture,
        val body: BodyFuture
    )

    @Schema(description = "Header 구성요소", example = "\"resultCode\":\"00\", \"resultMsg\":\"NORMAL_SERVICE\"")
    data class HeaderFuture(
        val resultCode: String,
        val resultMsg: String
    )

    @Schema(description = "Body 구성요소", example = "\"dataType\":\"JSON\", \"items\": {\"item\":{...")
    data class BodyFuture(
        val dataType: String,
        val items: ItemsFuture
    )

    @Schema(description = "item 리스트")
    data class ItemsFuture(
        val item: List<ItemFuture>
    )

    @Schema(description = "Item 구성요소", example = "\"baseDate\":\"20251226\", \"baseTime\":\"0600\", \"category\":\"PTY\", \"fcstDate\":\"20251226\", \"fcstTime\":\"0300\", \"fcstValue\":\"-12\", \"nx\":\"55\", \"ny\":\"127\"")
    data class ItemFuture(
        val baseDate: String,
        val baseTime: String,
        val category: String,
        val fcstDate: String,
        val fcstTime: String,
        val fcstValue: String,
        val nx: Int,
        val ny: Int
    )
}