package com.project.rainmind.weather.dto

data class KmaWeatherFutureFetchResponse (
    val response: TotalResponseFuture
) {
    data class TotalResponseFuture(
        val header: HeaderFuture,
        val body: BodyFuture
    )

    data class HeaderFuture(
        val resultCode: String,
        val resultMsg: String
    )

    data class BodyFuture(
        val dataType: String,
        val items: ItemsFuture
    )

    data class ItemsFuture(
        val item: List<ItemFuture>
    )

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