# Project : RainMind - 사용자 일정 기반 날씨 알림 서비스  
  
## 목차  
- 프로젝트 개요  
- 전체 아키텍처  
- 시스템 흐름  
- 기술 스택    
- 문제 정의  
- 기술적 의사결정  
- 트러블 슈팅 및 주요 코드 포인트  
- 실행 방법  
  
## 1. 프로젝트 개요  
RainMind는 일정(Schedule) 생성 시 알림을 예약하고, 지정된 시점의 날씨 알림 이벤트를 안정적으로 처리하기 위한 서버 애플리케이션입니다.  
  
알림 이벤트를 Redis에 저장함으로써 서비스 성능을 끌어올리고, 데이터 정합성 문제 해결을 위해 Outbox pattern을 적용하였습니다.  
  
본 프로젝트는 이후 FastAPI 기반, 동일한 알림 처리 서비스를 제공하는 프로젝트의 원본에 해당합니다. FastAPI 버전 Repository: https://github.com/LOV-ING-U/rainmind_fastapi  
  
## 2. 전체 아키텍처  
![arcihitecture](./img/rainmind.png)  
  
- Client 요청  
- Spring API Server 요청 수신  
- RDB (Schedule, Alarm Outbox) 삽입/삭제 및 Event 발생  
- Transactional Event Listener의 Event handle 및 Redis ZSet 조작  
- Alarm Worker의 alarm 출력, Client 응답 전송  
  
## 3. 시스템 흐름  
- 주요 로직 흐름  
1. Client가 일정 생성/삭제 API를 호출합니다.  
2. Schedule과 Alarm Outbox를 하나의 DB 트랜잭션으로 저장합니다.  
3. 트랜잭션 commit 이후, alarm event를 Redis ZSet에 enqueue합니다.  
4. Worker가 Lua Script를 이용한 atomic dequeue를 수행하여 알람을 출력합니다.  
  
- 보조 로직 흐름  
1. 사용자는 회원가입/로그인 기능을 우선적으로 이용하여 권한을 획득합니다.  
2. 사용자는 일정을 생성할 경우, 일정 시작 시각 이전에 알람을 출력받을 수 있습니다.  
3. 또는 미리 등록한 같은 날짜의 다른 일정이 시작하는 시각의 날씨 정보를 미리 받을 수 있습니다.  
  
## 4. 기술 스택  
<p>
    <img alt="spring boot"  src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white">
    <img alt="docker"  src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white">
    <img alt="redis"  src="https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white">
    <img alt="lua"  src="https://img.shields.io/badge/lua-%232C2D72.svg?style=for-the-badge&logo=lua&logoColor=white">
    <img alt="mysql"  src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">
</p>  
  
## 5. 문제 정의  
  
## 6. 기술적 의사결정  
  
## 7. 트러블 슈팅 및 주요 코드 포인트  
  
## 8. 실행 방법