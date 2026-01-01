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
        scheduleId: Long,
        notifyAtMs: Long
    ) {
        stringRedisTemplate.opsForZSet().add(ZSET_KEY, scheduleId.toString(), notifyAtMs.toDouble())
    }

    fun dequeueAlarm(
        scheduleId: Long
    ) {
        stringRedisTemplate.opsForZSet().remove(ZSET_KEY, scheduleId.toString())
    }

    // worker가 지금 당장 처리할 알람들 리스트
    // 모든 스케줄을 DB 검색? ㄴㄴ, redis sorted set score 이용하여 뽑음
    fun popAll(
        nowMs: Long
    ): List<Long> {
        // Set<String> : scheduleId 리스트
        val alarmSet = stringRedisTemplate.opsForZSet().rangeByScore(ZSET_KEY, 0.0, nowMs.toDouble()) ?: emptySet()

        // remove 반환형: 삭제된 멤버 개수
        if(!alarmSet.isEmpty()) stringRedisTemplate.opsForZSet().remove(ZSET_KEY, alarmSet)

        // string -> long 형변환
        return alarmSet.mapNotNull {
            it.toLongOrNull()
        }
    }
}