package com.project.rainmind

import com.project.rainmind.alarm.service.ALARM_QUEUE_KEY
import com.project.rainmind.alarm.service.NotifyQueueService
import com.project.rainmind.common.SEOUL
import com.project.rainmind.helper.BaseIntegrationTestContainers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.assertEquals

// Redis 통합 테스트: 발송 워커가 여러 대여도 같은 알림을 두 번 꺼내지 않는다(Lua 원자적 claim)
@SpringBootTest
@ActiveProfiles("test")
class NotifyQueueIntegrationTest
    @Autowired
    constructor(
        private val notifyQueueService: NotifyQueueService,
        private val stringRedisTemplate: StringRedisTemplate
    ): BaseIntegrationTestContainers() {

    @BeforeEach
    fun clear() {
        stringRedisTemplate.delete(ALARM_QUEUE_KEY)
    }

    @Test
    fun `여러 스레드가 동시에 꺼내도 같은 id 가 두 번 나오지 않는다`() {
        val total = 1000
        val due = LocalDateTime.now(SEOUL).minusMinutes(1)
        (1L..total.toLong()).forEach { notifyQueueService.enqueue(it, due) }

        val popped = Collections.synchronizedList(mutableListOf<Long>())
        val pool = Executors.newFixedThreadPool(8)
        val tasks = (1..8).map {
            Callable {
                while (true) {
                    val ids = notifyQueueService.popDue(50)
                    if (ids.isEmpty()) break
                    popped += ids
                }
            }
        }
        pool.invokeAll(tasks).forEach { it.get() }
        pool.shutdown()

        assertEquals(total, popped.size)
        assertEquals(total, popped.toSet().size)
        assertEquals(0L, stringRedisTemplate.opsForZSet().zCard(ALARM_QUEUE_KEY))
    }
}
