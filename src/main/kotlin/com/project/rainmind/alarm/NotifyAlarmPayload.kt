package com.project.rainmind.alarm

import java.time.LocalDateTime

data class NotifyAlarmPayload (
    val scheduleId: Long,
    val userId: Long,
    val title: String,
    val regionName: String,
    val nx: Int,
    val ny: Int,
    val startAt: LocalDateTime,
    val alarmAt: LocalDateTime,
    val outboxId: Long // 알림을 한번에 2개이상 등록할 경우, 멱등성에 의해 2개 이상의 알림은 무시될 수 있다.
    // 물론 그 경우, 알림 로직도 그에 맞춰(ex: 화목 11시 알림 -> 일정은 1개더라도 화/목 2번 울려야)
    // 바뀔 경우를 대비하여... 알람 아이디도 payload에 포함하여 JSON 문자열을 다르게 하면 좋을듯
)