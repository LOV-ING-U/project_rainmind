package com.project.rainmind.schedule

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "schedules")
class Schedule (
    @Id
    var id: Long? = null,
    @Column("user_id")
    var userId: Long,
    @Column("title")
    var title: String,
    @Column("start_at")
    var startAt: LocalDateTime,
    // 지역 정보는 고정값이라 일정에 직접 저장한다(발송 시 JOIN 없음)
    @Column("region_name")
    var regionName: String,
    @Column("nx")
    var nx: Int,
    @Column("ny")
    var ny: Int,
    // 알림 발송 시각. null 이면 아직 발송 전
    @Column("notified_at")
    var notifiedAt: LocalDateTime? = null
)
