package com.project.rainmind.common

import java.time.ZoneId

// 일정 시각은 모두 서울 시간 기준 LocalDateTime 으로 저장한다.
// DB 의 NOW() 는 DB 타임존에 따라 달라지므로, 비교용 현재 시각은 항상 앱에서 계산해 넘긴다.
val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")
