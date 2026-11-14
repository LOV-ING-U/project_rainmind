package com.project.rainmind.weather.dto

data class KmaWeatherFutureFetchResponse (
    val response: TotalResponseFuture
) {
    data class TotalResponseFuture(
        val header: HeaderFuture,
        // 에러 응답에는 body 가 없다
        val body: BodyFuture?
    )

    data class HeaderFuture(
        val resultCode: String,
        val resultMsg: String
    )

    data class BodyFuture(
        val dataType: String,
        // 데이터가 없으면 items 가 비어 올 수 있다
        val items: ItemsFuture?
    )

    data class ItemsFuture(
        val item: List<ItemFuture>
    )

    // 단기 예보 조회의 응답 메시지 명세 그대로
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