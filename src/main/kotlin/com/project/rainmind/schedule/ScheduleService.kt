package com.project.rainmind.schedule

import com.project.rainmind.schedule.dto.ScheduleCreateResponse
import com.project.rainmind.schedule.dto.ScheduleDeleteResponse
import com.project.rainmind.alarm.entity.AlarmOutbox
import com.project.rainmind.alarm.repository.AlarmOutboxRepository
import com.project.rainmind.common.SEOUL
import com.project.rainmind.region.InvalidRegionCodeException
import com.project.rainmind.region.RegionCatalog
import com.project.rainmind.user.NonExistingUsernameException
import com.project.rainmind.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

// 사용자당 최대 일정 수
private const val MAX_SCHEDULES_PER_USER = 40L
// 일정 시작 몇 분 전에 알림을 보낼지
private val ALARM_BEFORE: Duration = Duration.ofMinutes(30)

@Service
class ScheduleService (
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val alarmOutboxRepository: AlarmOutboxRepository,
    private val regionCatalog: RegionCatalog
) {
    @Transactional
    fun createSchedule(
        nickname: String,
        title: String,
        regionCode: String,
        startAt: OffsetDateTime
    ): ScheduleCreateResponse {
        // 1. 사용자 행 X 락. 같은 사용자의 동시 생성은 여기서 순서대로 처리된다.
        //    반드시 트랜잭션의 첫 읽기여야 한다. 일반 SELECT 가 먼저 오면 그 시점에 스냅샷이 잡혀,
        //    아래 COUNT 가 락 대기 중 커밋된 다른 요청의 일정을 보지 못한다.
        val user = userRepository.findByNicknameForUpdate(nickname) ?: throw NonExistingUsernameException()

        val region = regionCatalog.findByCode(regionCode) ?: throw InvalidRegionCodeException()
        if (!startAt.toInstant().isAfter(Instant.now())) throw InvalidScheduleStartTimeException()

        // 2. 일반 SELECT(락 없음, gap 락 없음). 사용자 락을 잡은 뒤라 같은 사용자의 요청이 끼어들 수 없다.
        if (scheduleRepository.countByUserId(user.id!!) >= MAX_SCHEDULES_PER_USER) throw TooManySchedulesException()

        // 3. 일정과 outbox 를 같은 트랜잭션에 저장
        val startAtSeoul = startAt.atZoneSameInstant(SEOUL).toLocalDateTime()
        val saved = scheduleRepository.save(
            Schedule(
                userId = user.id!!,
                title = title,
                startAt = startAtSeoul,
                regionName = region.name,
                nx = region.nx,
                ny = region.ny
            )
        )
        alarmOutboxRepository.save(
            AlarmOutbox(
                scheduleId = saved.id!!,
                alarmAt = startAtSeoul.minus(ALARM_BEFORE)
            )
        )

        return ScheduleCreateResponse(
            scheduleId = saved.id!!
        )
    }

    @Transactional
    fun deleteSchedule(
        nickname: String,
        scheduleId: Long
    ): ScheduleDeleteResponse {
        val user = userRepository.findByNickname(nickname) ?: throw NonExistingUsernameException()
        val schedule = scheduleRepository.findByIdAndUserId(scheduleId, user.id!!) ?: throw ScheduleNotFoundException()

        // 일정과 (아직 등록 전이라면) outbox 만 지운다.
        // Redis 는 건드리지 않는다. 큐에 남은 id 는 발송 직전 DB 확인에서 걸러진다.
        scheduleRepository.deleteById(schedule.id!!)
        alarmOutboxRepository.deleteByScheduleId(schedule.id!!)

        return ScheduleDeleteResponse(
            deletedScheduleId = schedule.id!!
        )
    }
}
