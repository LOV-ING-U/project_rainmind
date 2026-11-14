package com.project.rainmind.region

// 지역 코드, 이름, 기상청 격자 좌표
data class Region(
    val code: String,
    val name: String,
    val nx: Int,
    val ny: Int
)
