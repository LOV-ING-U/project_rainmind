package com.project.rainmind.weather.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("location")
class Location(
    @Id var id: Long? = null,
    @Column("region_name") var regionName: String,
    @Column("latitude") var latitude: Double,
    @Column("longitude") var longitude: Double,
    @Column("nx") var nx: Int,
    @Column("ny") var ny: Int,
    @Column("created_at") var createdAt: LocalDateTime,
    @Column("updated_at") var updatedAt: LocalDateTime
)