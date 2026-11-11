package com.project.rainmind

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.NotifyAlarmWorker
import com.project.rainmind.alarm.OutboxRelayWorker
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import com.project.rainmind.alarm.service.ALARM_QUEUE_KEY
import com.project.rainmind.common.SEOUL
import com.project.rainmind.helper.BaseIntegrationTestContainers
import com.project.rainmind.helper.RecordingAlarmSender
import com.project.rainmind.helper.RecordingAlarmSenderConfig
import com.project.rainmind.helper.SAMPLE_NX
import com.project.rainmind.helper.SAMPLE_NY
import com.project.rainmind.helper.SAMPLE_REGION_CODE
import com.project.rainmind.helper.createSchedule
import com.project.rainmind.helper.signUpAndLogin
import com.project.rainmind.helper.uniqueNickname
import com.project.rainmind.schedule.TooManySchedulesException
import com.project.rainmind.schedule.ScheduleRepository
import com.project.rainmind.schedule.ScheduleService
import com.project.rainmind.user.UserRepository
import com.project.rainmind.weather.WeatherForecast
import com.project.rainmind.weather.WeatherStore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.*

// 알림 등록 / 발송 / 삭제 / 동시 생성 / 날씨 통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RecordingAlarmSenderConfig::class)
class AlarmWorkerIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val objectMapper: ObjectMapper,
        private val scheduleRepository: ScheduleRepository,
        private val scheduleService: ScheduleService,
        private val userRepository: UserRepository,
        private val alarmOutboxRepository: AlarmOutboxRepository,
        private val stringRedisTemplate: StringRedisTemplate,
        private val jdbcTemplate: JdbcTemplate,
        private val outboxRelayWorker: OutboxRelayWorker,
        private val notifyAlarmWorker: NotifyAlarmWorker,
        private val weatherStore: WeatherStore,
        private val recordingAlarmSender: RecordingAlarmSender
    ): BaseIntegrationTestContainers() {

    private val queueKey = ALARM_QUEUE_KEY

    @BeforeEach
    fun clear() {
        stringRedisTemplate.delete(queueKey)
        recordingAlarmSender.sent.clear()
    }

    private fun score(scheduleId: Long): Double? =
        stringRedisTemplate.opsForZSet().score(queueKey, scheduleId.toString())

    // 알림 시각을 지금 이전으로 당겨서 바로 꺼내지게 한다
    private fun makeDue(scheduleId: Long) {
        stringRedisTemplate.opsForZSet().add(queueKey, scheduleId.toString(), (System.currentTimeMillis() - 1000).toDouble())
    }

    private fun newUserToken(): String = mvc.signUpAndLogin(objectMapper, uniqueNickname("alarm"))

    private fun startIn(minutes: Long): OffsetDateTime = OffsetDateTime.now(SEOUL).plusMinutes(minutes)

    @Test
    fun `생성하면 일정과 outbox 가 함께 저장되고, relay 후 큐에 id 가 들어가며 outbox 는 비워진다`() {
        val id = mvc.createSchedule(objectMapper, newUserToken(), startIn(120))

        assertTrue(scheduleRepository.existsById(id))
        assertEquals(1, alarmOutboxRepository.findAllByScheduleId(id).size)
        assertNull(score(id))

        outboxRelayWorker.relayOnce()

        assertNotNull(score(id))
        assertTrue(alarmOutboxRepository.findAllByScheduleId(id).isEmpty())
    }

    @Test
    fun `알림 시각 전에는 꺼내지지 않는다`() {
        val id = mvc.createSchedule(objectMapper, newUserToken(), startIn(120))
        outboxRelayWorker.relayOnce()

        notifyAlarmWorker.sendOnce()

        assertFalse(recordingAlarmSender.sentIds().contains(id))
        assertNotNull(score(id))
    }

    @Test
    fun `알림 시각이 되면 발송되고 notified_at 이 기록된다`() {
        val id = mvc.createSchedule(objectMapper, newUserToken(), startIn(40), title = "팀 회의")
        outboxRelayWorker.relayOnce()
        makeDue(id)

        notifyAlarmWorker.sendOnce()

        assertEquals(listOf(id), recordingAlarmSender.sentIds())
        val message = recordingAlarmSender.sent.first()
        assertEquals("팀 회의", message.title)
        assertTrue(message.render().contains("신림동"))
        assertNotNull(scheduleRepository.findById(id).orElseThrow().notifiedAt)
        assertNull(score(id))
    }

    @Test
    fun `삭제된 일정은 큐에 남아 있어도 발송되지 않는다`() {
        val token = newUserToken()
        val id = mvc.createSchedule(objectMapper, token, startIn(40))
        outboxRelayWorker.relayOnce()

        mvc.perform(delete("/schedules/$id").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
        // 삭제는 Redis 를 건드리지 않는다
        assertNotNull(score(id))

        makeDue(id)
        notifyAlarmWorker.sendOnce()

        assertFalse(recordingAlarmSender.sentIds().contains(id))
        assertNull(score(id))
    }

    @Test
    fun `등록 전에 삭제하면 outbox 도 함께 지워져 큐에 들어가지 않는다`() {
        val token = newUserToken()
        val id = mvc.createSchedule(objectMapper, token, startIn(40))

        mvc.perform(delete("/schedules/$id").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
        outboxRelayWorker.relayOnce()

        assertTrue(alarmOutboxRepository.findAllByScheduleId(id).isEmpty())
        assertNull(score(id))
    }

    @Test
    fun `이미 보낸 일정은 다시 큐에 들어가도 재발송되지 않는다`() {
        val id = mvc.createSchedule(objectMapper, newUserToken(), startIn(40))
        outboxRelayWorker.relayOnce()
        makeDue(id)
        notifyAlarmWorker.sendOnce()

        // 같은 id 재등록(예: outbox 재시도)
        makeDue(id)
        notifyAlarmWorker.sendOnce()

        assertEquals(1, recordingAlarmSender.sentIds().count { it == id })
    }

    @Test
    fun `시작 시각이 지난 일정은 발송하지 않는다`() {
        val id = mvc.createSchedule(objectMapper, newUserToken(), startIn(40))
        outboxRelayWorker.relayOnce()

        // 장애 등으로 늦게 꺼내진 상황: 시작 시각을 과거로 변경
        jdbcTemplate.update(
            "UPDATE schedules SET start_at = ? WHERE id = ?",
            Timestamp.valueOf(LocalDateTime.now(SEOUL).minusMinutes(5)), id
        )
        makeDue(id)
        notifyAlarmWorker.sendOnce()

        assertFalse(recordingAlarmSender.sentIds().contains(id))
    }

    @Test
    fun `같은 사용자가 동시에 생성해도 한도 40개를 넘지 않는다`() {
        val nickname = uniqueNickname("limit")
        mvc.signUpAndLogin(objectMapper, nickname)
        val userId = userRepository.findByNickname(nickname)!!.id!!

        repeat(35) {
            scheduleService.createSchedule(nickname, "일정 $it", SAMPLE_REGION_CODE, startIn(120))
        }

        // 남은 자리 5개에 10개를 동시에 요청
        val pool = Executors.newFixedThreadPool(10)
        val results = pool.invokeAll((1..10).map {
            Callable {
                try {
                    scheduleService.createSchedule(nickname, "동시 $it", SAMPLE_REGION_CODE, startIn(120))
                    "created"
                } catch (e: TooManySchedulesException) {
                    "rejected"
                }
            }
        }).map { it.get() }
        pool.shutdown()

        assertEquals(5, results.count { it == "created" })
        assertEquals(5, results.count { it == "rejected" })
        assertEquals(40L, scheduleRepository.countByUserId(userId))
    }

    @Test
    fun `날씨 예보가 있으면 메시지에 붙고, 없으면 정보 없음으로 발송된다`() {
        val withWeather = mvc.createSchedule(objectMapper, newUserToken(), startIn(40))
        val startAt = scheduleRepository.findById(withWeather).orElseThrow().startAt

        // 일정 시작 시각(정시)의 예보 저장(DB + Redis)
        val now = LocalDateTime.now(SEOUL)
        weatherStore.saveAll(
            listOf(
                WeatherForecast(
                    nx = SAMPLE_NX, ny = SAMPLE_NY,
                    fcstAt = startAt.truncatedTo(ChronoUnit.HOURS),
                    sky = 1, pty = 0, pop = 20, tmp = 3.5
                )
            ),
            baseAt = now,
            fetchedAt = now
        )

        outboxRelayWorker.relayOnce()
        makeDue(withWeather)
        notifyAlarmWorker.sendOnce()

        val message = recordingAlarmSender.sent.first { it.scheduleId == withWeather }
        assertNotNull(message.weather)
        assertTrue(message.render().contains("강수확률 20%"))

        // 예보가 없으면 날씨 없이 발송
        stringRedisTemplate.delete(stringRedisTemplate.keys("weather:*"))
        val withoutWeather = mvc.createSchedule(objectMapper, newUserToken(), startIn(40))
        outboxRelayWorker.relayOnce()
        makeDue(withoutWeather)
        notifyAlarmWorker.sendOnce()

        val message2 = recordingAlarmSender.sent.first { it.scheduleId == withoutWeather }
        assertNull(message2.weather)
        assertTrue(message2.render().contains("(정보 없음)"))
    }
}
