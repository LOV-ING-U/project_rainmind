package com.project.rainmind.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.alarm.sender.AlarmMessage
import com.project.rainmind.alarm.sender.AlarmSender
import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.dto.UserSignUpRequest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

// regions.csv 의 샘플 지역
const val SAMPLE_REGION_CODE = "0000000001"
const val SAMPLE_NX = 59
const val SAMPLE_NY = 125

private val seq = AtomicLong(System.currentTimeMillis())
fun uniqueNickname(prefix: String = "user"): String = "${prefix}_${seq.incrementAndGet()}"

// 발송된 알림을 기록하는 테스트용 sender
class RecordingAlarmSender : AlarmSender {
    val sent: MutableList<AlarmMessage> = CopyOnWriteArrayList()
    override fun send(message: AlarmMessage) {
        sent += message
    }
    fun sentIds(): List<Long> = sent.map { it.scheduleId }
}

@TestConfiguration
class RecordingAlarmSenderConfig {
    @Bean
    @Primary
    fun recordingAlarmSender(): RecordingAlarmSender = RecordingAlarmSender()
}

// 회원가입 후 로그인해서 토큰 반환
fun MockMvc.signUpAndLogin(objectMapper: ObjectMapper, nickname: String, password: String = "password"): String {
    perform(
        post("/auth/user/register")
            .content(objectMapper.writeValueAsString(UserSignUpRequest(nickname, password)))
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isCreated)

    val result = perform(
        post("/auth/user/login")
            .content(objectMapper.writeValueAsString(UserLogInRequest(nickname, password)))
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk).andReturn()

    return objectMapper.readValue(result.response.contentAsString, UserLogInResponse::class.java).token
}

// 일정 생성 후 id 반환
fun MockMvc.createSchedule(
    objectMapper: ObjectMapper,
    token: String,
    startAt: OffsetDateTime,
    title: String = "프로젝트 미팅",
    regionCode: String = SAMPLE_REGION_CODE
): Long {
    val result = perform(
        post("/schedules")
            .header("Authorization", "Bearer $token")
            .content(objectMapper.writeValueAsString(ScheduleCreateRequest(title, regionCode, startAt)))
            .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(status().isCreated).andReturn()

    return objectMapper.readValue(result.response.contentAsString, ScheduleCreateResponse::class.java).scheduleId
}
