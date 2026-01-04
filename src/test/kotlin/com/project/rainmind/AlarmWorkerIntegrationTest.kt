package com.project.rainmind

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.NotifyAlarmWorker
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import com.project.rainmind.helper.BaseIntegrationTestContainers
import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import com.project.rainmind.schedule.repository.ScheduleRepository
import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.dto.UserSignUpRequest
import com.project.rainmind.user.repository.UserLogInRepository
import com.project.rainmind.weather.entity.Location
import com.project.rainmind.weather.repository.LocationRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDateTime
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension::class)
class AlarmWorkerIntegrationTest
    @Autowired
    constructor(
        private val locationRepository: LocationRepository,
        private val userLogInRepository: UserLogInRepository,
        private val scheduleRepository: ScheduleRepository,
        private val mvc: MockMvc,
        private val objectMapper: ObjectMapper,
        private val alarmOutboxRepository: AlarmOutboxRepository,
        private val stringRedisTemplate: StringRedisTemplate,
        private val notifyAlarmWorker: NotifyAlarmWorker
        ): BaseIntegrationTestContainers() {
    private val username = "test-user-name"
    private val password = "test-password"
    private val ZSET_KEY = "alarm:queue"

    @Test
    // schedule 생성, outbox 생성, redis enqueue, worker 처리
    // schedule delete, outbox 상태 변화, redis dequeue, worker 처리
    fun `make schedule, make outbox, enqueue redis, call worker, delete schedule, state change outbox, dequeue redis, call worker`(
        output: CapturedOutput
    ) {
        // location 등록
        val location = locationRepository.save(
            Location(
                regionName = "서울대학교",
                latitude = 37.49,
                longitude = 126.93,
                nx = 59,
                ny = 125,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val now = LocalDateTime.now()

        // 회원가입 & 로그인
        val token = signUpAndLogin(username, password, "서울대학교")

        // 일정 생성, 등록
        val scheduleCreateRequest = ScheduleCreateRequest(
            title = "프로젝트 미팅",
            locationId = location.id!!,
            startAt = now.plusMinutes(40),
            endAt = now.plusMinutes(50)
        )

        val scheduleCreateResult = mvc.perform(
            post("/v1/schedules")
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(scheduleCreateRequest))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated).andReturn()

        val scheduleCreateResponse = objectMapper.readValue(scheduleCreateResult.response.contentAsString, ScheduleCreateResponse::class.java)
        val scheduleId = scheduleCreateResponse.scheduleId

        // schedule, outbox DB 확인
        assertTrue(scheduleRepository.existsById(scheduleId))
        println("register complete on schedule table")

        assertTrue(alarmOutboxRepository.existsByScheduleId(scheduleId))
        println("register complete on alarm outbox table")

        // redis 검사
        // payload = JSON 문자열
        val payload = alarmOutboxRepository.findAllByScheduleId(scheduleId).first().payload
        val redisHasThisAlarm = stringRedisTemplate.opsForZSet().score(ZSET_KEY, payload)

        assertTrue(redisHasThisAlarm != null)
        println("score = " + redisHasThisAlarm)

        // redis 에서 즉시 dequeue 되도록 조정
        val score = (System.currentTimeMillis() - 1000).toDouble()
        stringRedisTemplate.opsForZSet().add(ZSET_KEY, payload, score)

        notifyAlarmWorker.runOnceOnTest()

        val out = output.out
        println(out)

        // schedule 삭제 체크
        // schedule 만듬
        val scheduleCreateRequest2 = ScheduleCreateRequest(
            title = "동아리 회의",
            locationId = location.id!!,
            startAt = now.plusMinutes(50),
            endAt = now.plusMinutes(60)
        )

        val scheduleCreateResult2 = mvc.perform(
            post("/v1/schedules")
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(scheduleCreateRequest2))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated).andReturn()

        // 생성했다가 삭제하기
        val scheduleCreateResponse2 = objectMapper.readValue(scheduleCreateResult2.response.contentAsString, ScheduleCreateResponse::class.java)
        val scheduleId2 = scheduleCreateResponse2.scheduleId
        val payload2 = alarmOutboxRepository.findAllByScheduleId(scheduleId2).first().payload

        // 일단 생성했으니 redis 에 있는지 확인
        val redisHasThisAlarm2 = stringRedisTemplate.opsForZSet().score(ZSET_KEY, payload2)

        assertTrue(redisHasThisAlarm2 != null)
        println("score = " + redisHasThisAlarm2)

        // 삭제
        mvc.perform(
            delete("/v1/schedules/$scheduleId2")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)

        // schedule DB 에서 삭제되었는지, 그리고 outbox 에서 deleted state 되었는지 확인
        assertFalse(scheduleRepository.existsById(scheduleId2))
        println("successfully deleted at schedule DB")

        val outbox2 = alarmOutboxRepository.findAllByScheduleId(scheduleId2)
        assertTrue(outbox2.all { it ->
            it.status.name == "DELETED"
        })

        // redis 에서 삭제되었는지 확인
        val stillRedis = stringRedisTemplate.opsForZSet().score(ZSET_KEY, payload2)
        assertTrue(stillRedis == null)

        // worker 실행 후에도 출력이 없어야함
        notifyAlarmWorker.runOnceOnTest()

        val out2 = output.out
        println(out2)
    }

    // return login token
    private fun signUpAndLogin(
        nickname: String,
        password: String,
        regionName: String
    ): String {
        val userSignUpRequest = UserSignUpRequest(
            nickname = nickname,
            password = password,
            region_name = regionName
        )

        mvc.perform(
            post("/v1/auth/user/register")
                .content(objectMapper.writeValueAsString(userSignUpRequest))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated)

        val userLogInRequest = UserLogInRequest(
            nickname = nickname,
            password = password
        )

        val logInResult = mvc.perform(
            post("/v1/auth/user/login")
                .content(objectMapper.writeValueAsString(userLogInRequest))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk).andReturn()

        val token = objectMapper.readValue(logInResult.response.contentAsString, UserLogInResponse::class.java).token
        return token
    }
}
