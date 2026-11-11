package com.project.rainmind

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.common.SEOUL
import com.project.rainmind.helper.BaseIntegrationTestContainers
import com.project.rainmind.helper.SAMPLE_REGION_CODE
import com.project.rainmind.helper.createSchedule
import com.project.rainmind.helper.signUpAndLogin
import com.project.rainmind.helper.uniqueNickname
import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.dto.UserSignUpRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

// 회원 / 지역 / 일정 API 통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val objectMapper: ObjectMapper
    ): BaseIntegrationTestContainers() {

        @Test
        fun `회원가입, 로그인, 로그아웃 후 같은 토큰은 거부되고 재로그인하면 다시 접근된다`() {
            val nickname = uniqueNickname("hello")
            val password = "whatwhat"
            val token = mvc.signUpAndLogin(objectMapper, nickname, password)

            // 없는 일정 삭제 요청: 인증되면 404
            mvc.perform(delete("/schedules/0").header("Authorization", "Bearer $token"))
                .andExpect(status().isNotFound)

            mvc.perform(post("/user/logout").header("Authorization", "Bearer $token"))
                .andExpect(status().isOk)

            // 로그아웃된 토큰(블랙리스트)으로 접근
            mvc.perform(delete("/schedules/0").header("Authorization", "Bearer $token"))
                .andExpect(status().isUnauthorized)

            // 재로그인
            val result = mvc.perform(
                post("/auth/user/login")
                    .content(objectMapper.writeValueAsString(UserLogInRequest(nickname, password)))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()
            val token2 = objectMapper.readValue(result.response.contentAsString, UserLogInResponse::class.java).token

            mvc.perform(delete("/schedules/0").header("Authorization", "Bearer $token2"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `닉네임이나 비밀번호가 4자 미만이면 400`() {
            mvc.perform(
                post("/auth/user/register")
                    .content(objectMapper.writeValueAsString(UserSignUpRequest("hel", "whatwhat")))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest)

            mvc.perform(
                post("/auth/user/register")
                    .content(objectMapper.writeValueAsString(UserSignUpRequest(uniqueNickname("hello"), "wha")))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `로그인하지 않으면 일정을 만들 수 없다`() {
            val request = ScheduleCreateRequest("미팅", SAMPLE_REGION_CODE, OffsetDateTime.now(SEOUL).plusHours(1))
            mvc.perform(
                post("/schedules")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isUnauthorized)
        }

        @Test
        fun `없는 지역 코드나 과거 시작 시각이면 400`() {
            val token = mvc.signUpAndLogin(objectMapper, uniqueNickname("invalid"))

            val wrongRegion = ScheduleCreateRequest("미팅", "9999999999", OffsetDateTime.now(SEOUL).plusHours(1))
            mvc.perform(
                post("/schedules")
                    .header("Authorization", "Bearer $token")
                    .content(objectMapper.writeValueAsString(wrongRegion))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest)

            val past = ScheduleCreateRequest("미팅", SAMPLE_REGION_CODE, OffsetDateTime.now(SEOUL).minusMinutes(1))
            mvc.perform(
                post("/schedules")
                    .header("Authorization", "Bearer $token")
                    .content(objectMapper.writeValueAsString(past))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `다른 사용자의 일정은 삭제할 수 없고, 본인 일정은 삭제된다`() {
            val token1 = mvc.signUpAndLogin(objectMapper, uniqueNickname("owner"))
            val token2 = mvc.signUpAndLogin(objectMapper, uniqueNickname("other"))

            val scheduleId = mvc.createSchedule(objectMapper, token1, OffsetDateTime.now(SEOUL).plusHours(1))

            mvc.perform(delete("/schedules/$scheduleId").header("Authorization", "Bearer $token2"))
                .andExpect(status().isNotFound)

            mvc.perform(delete("/schedules/$scheduleId").header("Authorization", "Bearer $token1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.deletedScheduleId").value(scheduleId))
        }
    }
