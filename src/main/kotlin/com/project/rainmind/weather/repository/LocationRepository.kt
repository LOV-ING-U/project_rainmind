package com.project.rainmind.weather.repository

import com.project.rainmind.weather.entity.Location
import org.springframework.data.repository.ListCrudRepository

interface LocationRepository : ListCrudRepository<Location, Long> {
    fun findByRegionName(
        regionName: String
    ): Location?
}