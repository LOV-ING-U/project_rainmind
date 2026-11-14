package com.project.rainmind.alarm.service

import com.project.rainmind.common.SEOUL
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.LocalDateTime

// 테스트에서도 참조하는 큐 키
const val ALARM_QUEUE_KEY = "alarm:queue"

// 알림 시각이 지난 원소를 꺼내면서 동시에 제거한다(원자적 claim).
// 워커가 여러 대여도 같은 id 를 두 번 꺼내지 않는다.
private const val POP_LUA = """
    local items = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', ARGV[1], 'LIMIT', 0, tonumber(ARGV[2]))
    if #items > 0 then
        redis.call('ZREM', KEYS[1], unpack(items))
    end
    return items
"""

// Redis ZSET 알림 큐
// member = scheduleId, score = 알림 시각(epoch ms)
// 원본은 DB 이고, 이 큐는 언제든 DB 에서 다시 만들 수 있는 시간순 인덱스다.
@Service
class NotifyQueueService (
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val popScript: DefaultRedisScript<List<*>> = DefaultRedisScript<List<*>>().apply {
        resultType = List::class.java
        setScriptText(POP_LUA.trimIndent())
    }

    // 같은 id 를 여러 번 넣어도 score 만 갱신된다(멱등)
    fun enqueue(
        scheduleId: Long,
        alarmAt: LocalDateTime
    ) {
        val score = alarmAt.atZone(SEOUL).toInstant().toEpochMilli().toDouble()
        stringRedisTemplate.opsForZSet().add(ALARM_QUEUE_KEY, scheduleId.toString(), score)
    }

    // 현재 시각까지 도래한 알림 id 를 최대 count 개 꺼낸다
    fun popDue(
        count: Int
    ): List<Long> {
        val result = stringRedisTemplate.execute(
            popScript,
            listOf(ALARM_QUEUE_KEY),
            System.currentTimeMillis().toString(),
            count.toString()
        )
        return result?.mapNotNull { it?.toString()?.toLongOrNull() } ?: emptyList()
    }
}
