package com.project.rainmind

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.common.SEOUL
import com.project.rainmind.helper.BaseIntegrationTestContainers
import com.project.rainmind.helper.SAMPLE_REGION_CODE
import com.project.rainmind.helper.createSchedule
import com.project.rainmind.helper.signUpAndLogin
import com.project.rainmind.helper.uniqueNickname
import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

// 인증 / 권한 통합 테스트: 남의 일정은 건드릴 수 없다
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
        fun `로그인하지 않으면 일정을 만들 수 없다`() {
            val request = ScheduleCreateRequest("미팅", SAMPLE_REGION_CODE, OffsetDateTime.now(SEOUL).plusHours(1))
            mvc.perform(
                post("/schedules")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isUnauthorized)
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
