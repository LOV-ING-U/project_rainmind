package com.project.rainmind.weather.weatherfetch.repository

import com.project.rainmind.weather.weatherfetch.entity.Location
import org.springframework.data.repository.ListCrudRepository

interface LocationRepository : ListCrudRepository<Location, Long> {
    fun findByRegionName(
        regionName: String
    ): Location?
}