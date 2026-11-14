package com.project.rainmind.region

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

// 기상청 행정구역-격자 목록(regions.csv)을 앱 시작 시 메모리에 올린다.
// 정적인 참조 데이터라 DB 테이블로 두지 않는다.
@Component
class RegionCatalog {
    private val regions: List<Region> = load()
    private val byCode: Map<String, Region> = regions.associateBy { it.code }

    fun findByCode(code: String): Region? = byCode[code]

    // 날씨 수집 대상 격자(중복 제거)
    // set of (nx, ny)
    fun grids(): Set<Pair<Int, Int>> = regions.map { it.nx to it.ny }.toSet()

    private fun load(): List<Region> =
        ClassPathResource("regions.csv").inputStream.bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.drop(1)
                .filter { it.isNotBlank() }
                .map { line ->
                    val cols = line.split(",")
                    Region(
                        code = cols[0].trim(),
                        name = cols[1].trim(),
                        nx = cols[2].trim().toInt(),
                        ny = cols[3].trim().toInt()
                    )
                }.toList()
        }
}
