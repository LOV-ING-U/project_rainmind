package com.project.rainmind.alarm.sender

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogAlarmSender : AlarmSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(message: AlarmMessage) {
        log.info("[알림] scheduleId={}, userId={}\n{}", message.scheduleId, message.userId, message.render())
    }
}
