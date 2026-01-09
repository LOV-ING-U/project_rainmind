# Project : RainMind - 사용자 일정 기반 날씨 알림 서비스  
  
## 목차  
- 프로젝트 개요  
- 전체 아키텍처  
- 기술 스택    
- 문제 정의   
- 시스템 흐름  
- 기술적 의사결정  
- 트러블 슈팅  
- 실행 방법  
  
## 1. 프로젝트 개요  
RainMind는 일정(Schedule) 생성 시 알림을 예약하고, 지정된 시점에 알림 이벤트를 안정적으로 처리하기 위한 서버 애플리케이션입니다.  
  
알림 이벤트를 Redis에 저장함으로써 서비스 성능을 끌어올리고, 데이터 정합성 문제 해결을 위해 Outbox pattern을 적용하였습니다.  
  
본 프로젝트는 이후 FastAPI 기반, 동일한 알림 처리 서비스를 제공하는 프로젝트의 원본에 해당합니다. FastAPI 버전 Repository: https://github.com/LOV-ING-U/rainmind_fastapi  
  
## 2. 