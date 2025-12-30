package com.project.rainmind

import com.project.rainmind.helper.BaseIntegrationTestContainers
import com.project.rainmind.user.dto.UserLogInRequest
import com.project.rainmind.user.dto.UserLogInResponse
import com.project.rainmind.user.dto.UserSignUpRequest
import com.project.rainmind.weather.entity.Location
import com.project.rainmind.weather.repository.LocationRepository
import com.project.rainmind.weather.repository.WeatherForecastRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.rainmind.schedule.dto.ScheduleCreateRequest
import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BaseIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc, // 실제 서버 띄우지 않고도 우리 컨트롤러에 http 요청 보내는 것처럼 시뮬레이션
        private val objectMapper: ObjectMapper, // JSON <-> DTO 변환 매핑
        private val locationRepository: LocationRepository,
        private val weatherForecastRepository: WeatherForecastRepository
    ): BaseIntegrationTestContainers() {
        @Test
        fun `successful register and login and logout, with calling expired token`() {
            val nickname = "hello_${System.currentTimeMillis()}"
            val password = "whatwhat"
            val regionName = "서울대학교"

            // location 등록
            locationRepository.save(
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

            // register
            val userSignUpRequest = UserSignUpRequest(nickname, password, regionName)
            mvc.perform(
                post("/v1/auth/user/register")
                    .content(objectMapper.writeValueAsString(userSignUpRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated)

            // login
            val userLogInRequest = UserLogInRequest(nickname, password)
            val loginResult = mvc.perform(
                post("/v1/auth/user/login")
                    .content(objectMapper.writeValueAsString(userLogInRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()

            // token 추출, logout 테스트
            val token = objectMapper.readValue(loginResult.response.contentAsString, UserLogInResponse::class.java).token
            val bearer_token = "Bearer $token"
            mvc.perform(
                post("/v1/user/logout")
                    .header("Authorization", bearer_token)
            ).andExpect(status().isOk)

            // logout 된 토큰으로 날씨 정보 받는 api(로그인 유저만 가능한) 호출 시도
            mvc.perform(
                get("/v1/weather/forecast")
                    .header("Authorization", bearer_token)
                    .param("regionName", regionName)
                    .param("date", "2025-12-24")
                    .param("time", "02:00:00")
            ).andExpect(status().isUnauthorized)

            // 재로그인
            val userLogInRequest2 = UserLogInRequest(nickname, password)
            val loginResult2 = mvc.perform(
                post("/v1/auth/user/login")
                    .content(objectMapper.writeValueAsString(userLogInRequest2))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()

            // 초단기실황조회 호출
            val token2 = objectMapper.readValue(loginResult2.response.contentAsString, UserLogInResponse::class.java).token
            val bearer_token2 = "Bearer $token2"

            val now = LocalDateTime.now()
            val (baseDate, baseTime) = now.minusHours(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) to
                    now.minusHours(1).format(DateTimeFormatter.ofPattern("HH")) + "00"

            mvc.perform(
                get("/v1/weather/current")
                    .header("Authorization", bearer_token2)
                    .param("baseDate", baseDate)
                    .param("baseTime", baseTime)
                    .param("regionName", regionName)
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.regionName").value(regionName))
                .andExpect(jsonPath("$.rn1").exists())
                .andExpect(jsonPath("$.pty").exists())
                .andExpect(jsonPath("$.wsd").exists())

            // 단기예보조회 호출(DB 저장)
            mvc.perform(
                get("/v1/weather/today")
                    .header("Authorization", bearer_token2)
                    .param("regionName", regionName)
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.storeCount").exists())

            // 실제로 잘 저장되었는지 테스트 & 임의로 해당 시각에 데이터 존재하는지 DB 에서 꺼내봄
            val location = locationRepository.findByRegionName(regionName)!!
            val allItems = weatherForecastRepository.findAll().filter {
                it.regionCode == location.id
            }
            assert(allItems.isNotEmpty())

            val firstItem = allItems.first()
            val date = firstItem.fcstDateAndTime.toLocalDate().toString()
            val time = firstItem.fcstDateAndTime.toLocalTime().toString()
            mvc.perform(
                get("/v1/weather/forecast")
                    .header("Authorization", bearer_token2)
                    .param("regionName", regionName)
                    .param("date", date)
                    .param("time", time)
            ).andExpect(status().isOk)
        }

        @Test
        fun `return 400 error when username less than 4`() {
            val nickname = "hel"
            val password = "whatwhat"
            val regionName = "서울대학교"

            val userSignUpRequest = UserSignUpRequest(nickname, password, regionName)
            mvc.perform(
                post("/v1/auth/user/register")
                    .content(objectMapper.writeValueAsString(userSignUpRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `return 400 error when password less than 4`() {
            val nickname = "hello"
            val password = "wha"
            val regionName = "서울대학교"

            val userSignUpRequest = UserSignUpRequest(nickname, password, regionName)
            mvc.perform(
                post("/v1/auth/user/register")
                    .content(objectMapper.writeValueAsString(userSignUpRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `two users register, create schedule, approach other user schedule and denied, delete schedule`() {
            val regionName = "서울대학교"

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

            // user1, 2 create
            val nickname1 = "iamuser1"
            val password1 = "iamuser1password"
            val user1SignUpRequest = UserSignUpRequest(nickname1, password1, regionName)
            mvc.perform(
                post("/v1/auth/user/register")
                    .content(objectMapper.writeValueAsString(user1SignUpRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated)

            val nickname2 = "iamuser1"
            val password2 = "iamuser1password"
            val user2SignUpRequest = UserSignUpRequest(nickname2, password2, regionName)
            mvc.perform(
                post("/v1/auth/user/register")
                    .content(objectMapper.writeValueAsString(user2SignUpRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated)

            // user1 create schedule(not login)
            val schedule1 = ScheduleCreateRequest(
                title = "프로젝트 미팅",
                locationId = location.id!!,
                startAt = LocalDateTime.now(),
                endAt = LocalDateTime.now().plusMinutes(30)
            )
            mvc.perform(
                post("/v1/schedules/create")
                    .content(objectMapper.writeValueAsString(schedule1))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk)

            // user1, 2 login and create schedule each 1
            // user1
            val user1LogInRequest = UserLogInRequest(nickname1, password1)
            val loginResult1 = mvc.perform(
                post("/v1/auth/user/login")
                    .content(objectMapper.writeValueAsString(user1LogInRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()
            val user1token = objectMapper.readValue(loginResult1.response.contentAsString, UserLogInResponse::class.java).token

            val user1scheduleCreateResponse = mvc.perform(
                post("/v1/schedules/create")
                    .header("Authorization", "Bearer $user1token")
                    .content(objectMapper.writeValueAsString(schedule1))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated).andReturn()

            // user2
            val user2LogInRequest = UserLogInRequest(nickname2, password2)
            val loginResult2 = mvc.perform(
                post("/v1/auth/user/login")
                    .content(objectMapper.writeValueAsString(user2LogInRequest))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()
            val user2token = objectMapper.readValue(loginResult2.response.contentAsString, UserLogInResponse::class.java).token

            val schedule2 = ScheduleCreateRequest(
                title = "학과 수강지도",
                locationId = location.id!!,
                startAt = LocalDateTime.now().plusHours(1),
                endAt = LocalDateTime.now().plusHours(2)
            )
            mvc.perform(
                post("/v1/schedules/create")
                    .header("Authorization", "Bearer $user2token")
                    .content(objectMapper.writeValueAsString(schedule2))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated)

            // user2 tries to delete user1 schedule
            // expected : not found
            val user1scheduleId = objectMapper.readValue(user1scheduleCreateResponse.response.contentAsString, ScheduleCreateResponse::class.java).scheduleId
            mvc.perform(
                delete("/v1/schedules/$user1scheduleId")
                    .header("Authorization", "Bearer $user2token")
            ).andExpect(status().isNotFound)

            // user1 tries to delete user1 schedule
            mvc.perform(
                delete("/v1/schedules/$user1scheduleId")
                    .header("Authorization", "Bearer $user1token")
            ).andExpect(status().isOk).andExpect(jsonPath("$.deletedScheduleId").value(user1scheduleId))
        }
    }