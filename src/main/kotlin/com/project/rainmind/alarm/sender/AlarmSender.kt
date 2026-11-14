package com.project.rainmind.alarm.sender

// 알림 발송 추상화. 실제 푸시 발송은 범위 밖이라 현재 구현체는 로그 출력뿐이다.
interface AlarmSender {
    fun send(message: AlarmMessage)
}
