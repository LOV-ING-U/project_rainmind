package com.project.rainmind.alarm

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class NotifyQueueService (
    private val stringRedisTemplate: StringRedisTemplate
) {
    // redis zset 사용
    // key: 알림 모음(= alarm:at 고정)
    // value: 알림들 집합(각 알람들은 이름/score 로 구성됨)
    // score 정렬된 상태로 유지(sorted set)
    private val ZSET_KEY = "alarm:at"

    fun enqueueAlarm(

    ) {

    }

    fun dequeueAlarm(

    ) {

    }
}