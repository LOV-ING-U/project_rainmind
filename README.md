# Project : RainMind - 일정 30분 전 날씨 알림 서비스

일정을 등록하면 시작 30분 전에 일정·장소·날씨를 담은 알림을 보내는 서버입니다.
DB 와 Redis 사이의 정합성(유실·유령·중복 알림)을 어떻게 보장하는지에 초점을 둔 프로젝트입니다.

## 1. 기술 스택
<p>
    <img alt="spring boot"  src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white">
    <img alt="docker"  src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white">
    <img alt="redis"  src="https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white">
    <img alt="lua"  src="https://img.shields.io/badge/lua-%232C2D72.svg?style=for-the-badge&logo=lua&logoColor=white">
    <img alt="mysql"  src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">
    <img alt="k6"  src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">
    <img alt="Grafana"  src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=Grafana&logoColor=white">
</p>

## 2. API

| 메서드 | 경로 | 인증 | 설명 |
| --- | --- | --- | --- |
| POST | `/auth/user/register` | X | 회원가입 (`nickname`, `password`) |
| POST | `/auth/user/login` | X | 로그인, JWT 발급 (IP 기반 실패 횟수 제한) |
| POST | `/user/logout` | O | 토큰 블랙리스트 등록 |
| POST | `/schedules` | O | 일정 생성 (`title`, `regionCode`, `startAt`) |
| DELETE | `/schedules/{id}` | O | 일정 삭제 |

- 일정 생성 조건: 시작 시각은 미래, 지역 코드는 지역 목록에 존재, 사용자당 최대 40개

## 3. 설계

### 전제
- **원본은 DB 입니다.** Redis 의 알림 큐와 날씨 사본은 DB 에서 다시 만들 수 있습니다(Redis 유실 복구 로직은 추후 구현).
- **`schedules` 는 쓰기가 많은 테이블로 가정합니다.** 가장 간단한 방법으로 DB 폴링이 있습니다만, 2초마다 '알림 시각이 지났으며 아직 보내지 않은 일정'을 찾기 위해서는 (send_time, status) 인덱스가 있어야 하며 쓰기가 많은 테이블로 가정했기 때문에 이 쓰기 비용을 줄이기 위해 Redis ZSET을 이용하여 시간순 정렬을 사용합니다. DB에는 꼭 필요한 PK / FK만 둡니다.
- **Redis 큐에서 꺼낸(pop) 알림은 반드시 발송된다고 가정합니다.**
  
### 데이터
| 위치 | 구성 |
| --- | --- |
| `users` | id, nickname, password_hash |
| `schedules` | id, user_id(FK), title, start_at, region_name, nx, ny, notified_at |
| `alarm_outbox` | id, schedule_id, alarm_at — Redis 에 아직 등록되지 않은 알림만 |
| `weather_forecast` | PK(nx, ny, fcst_at), sky, pty, pop, tmp, base_at, fetched_at |
| Redis `alarm:queue` | ZSET, member = schedule_id, score = 알림 시각 |
| Redis `weather:{nx}:{ny}:{yyyyMMddHH}` | 날씨 사본(`sky,pty,pop,tmp`), TTL = 예보 시각 + 1시간 |
| 메모리 | 지역 목록(`regions.csv`) |

### 흐름
```
[일정 생성]  TX { users FOR UPDATE → COUNT(한도 40) → INSERT schedules, INSERT alarm_outbox }
[등록 워커]  10초마다 outbox → ZADD(schedule_id) → 성공한 outbox 삭제
[발송 워커]  2초마다 Lua pop(ZRANGEBYSCORE + ZREM)
             → SELECT schedules WHERE id IN (…) AND notified_at IS NULL AND start_at > now  (PK 조회)
             → Redis 에서 날씨 조회 → 발송 → UPDATE notified_at
[일정 삭제]  TX { DELETE schedules, DELETE alarm_outbox }  (Redis 는 건드리지 않음)
[날씨 수집]  12시간마다 격자별 단기예보 조회 → DB(weather_forecast) upsert + Redis 저장
```

알림 메시지 예시
```
[RainMind] 30분 뒤 일정이 있어요
· 일정: 팀 회의
· 시간: 2026-11-12 15:00
· 장소: 서울특별시 관악구 신림동
· 날씨: 맑음, 강수확률 20%, 기온 3.5℃
```

### 설계 방식의 보장
| 항목 | 보장 | 근거 |
| --- | --- | --- |
| 등록 유실 | 없음 | 일정과 outbox 를 한 트랜잭션에 저장하고, 등록 워커에서 ZADD 성공 후에만 outbox 를 지움 |
| 유령 알림(삭제된 일정) | 없음 | 큐에는 id 만 두고, 발송 직전 PK 로 존재를 확인함 |
| 중복 발송 | Redis Deq 이후, `notified_at`을 기록하기 직전 서버가 Down 되었으며, 재시도 워커가 남은 outbox를 보고 재삽입한 경우에 가능 | - |
| 늦은 발송 | 시작이 지난 일정은 보내지 않음 | 발송 조회에 `start_at > now` 조건 |
| 같은 사용자 동시 생성 | 동시성 이슈 및 데드락 없음 | USER 행을 X-Lock을 잡아(비관적 락) & 일정을 삽입함 |
| 날씨 장애 | 알림은 정시 발송 | Redis에 날씨 정보가 없다면 생략함 |

### 회고
- 처음에는 시간순 정렬이 필요하다는 이유로 Redis ZSET 을 바로 도입했으나, 이 정도 규모에서는 DB 폴링으로도 충분했습니다. Redis를 넣으며 오히려 설계 복잡도가 증가했습니다.  
- 따라서 Redis ZSET 선택이 유효해지는 조건은 "쓰기 집중 테이블에서 2초 주기의 (시간, status) 인덱스 검색 비용을 줄이고 싶을때"입니다.  

## 4. 확장 설계 (아직 미구현된 부분)
- **발송 신뢰성**: Lua pop 과 Redis Streams `XADD` 를 한 스크립트로 묶고, consumer group + `XACK` + DLQ 로 발송. 외부 푸시에는 scheduleId 멱등 키
- **Redis 유실 복구**: 표시 키가 사라지면 미발송 일정을 DB 에서 다시 ZADD

## 5. 지역 데이터
`src/main/resources/regions.csv` (`code,name,nx,ny`) 를 앱 시작 시 메모리에 올립니다.
공공데이터포털 기상청 단기예보 조회서비스의 격자 위경도 자료(2026-09-30 기준)에서 행정구역코드, 시/군/구, 격자 X/Y 만 추려 만들었습니다.
총 3,839개 지역, 중복을 제거한 격자는 1,632개입니다. 날씨 수집은 격자 단위로 하루 2회 호출하므로 하루 약 3,300건입니다(API 일일 1만회 이하 제약으로 인해 보수적으로 설정).

## 6. 실행 방법
```cli
podman machine start
./gradlew test
```

- 날씨 수집은 기상청 인증키(`KMA_AUTH_KEY`)가 있어야 동작합니다. 없으면 수집이 실패하고, 알림은 날씨 없이 발송됩니다.

## 7. 기술 블로그
설계 과정과 트러블슈팅: https://lov-ing-u.github.io/categories/rainmind

## 8. k6 성능 테스트 (리팩터링 이전 버전에서 측정)  
1. **회원가입 기능 응답시간 최적화**  
- BCrypt 연산으로 인한 CPU 병목 파악, p(95) 최대 10배 개선  
- 대안으로 BCrypt gensalt() 인자값 설정, Argon2와 같은 대체 암호화 알고리즘 제안  
  
| 개선 전/후 | avg | p(90) | p(95) | max | RPS(req / s) |   
| --------- | --- | ----- | ----- | --- | ------------ |  
| 개선 전 | 3.68s | 3.91s | 3.96s | 6.14s | 27.044926 |  
| 개선 후 | 153.89ms | 344.7ms | 415.77ms | 1.44s | 648.711365 |  

2. **로그인 기능 응답시간 및 DB Connection Pool 최적화**  
- 무작위 로그인 공격으로 인한 내부 CPU 자원 및 DB 커넥션 풀 고갈 고려, Redis(외부 저장소) 기반 IP 캐싱 활용, 평균/p(90)/p(95) 응답시간 / RPS / DB 커넥션 풀 3배 이상 개선  
  
| 개선 전/후 | avg | p(90) | p(95) | max | RPS(req / s) | active DB connection(개 / min) |   
| --------- | --- | ----- | ----- | --- | ------------ | ------------------------------ |  
| 개선 전 | 223.88ms | 455.8ms | 602.69ms | 1.42s | 851.823838 | 약 3.83 |   
| 개선 후 | 64.09ms | 123.08ms | 180.17ms | 3.01s | 2669.172776 | 1.08 |  
  
3. **Schedule 생성 기능 응답시간 및 Heap memory, DB Connection Pool 최적화**  
- Native Query를 이용한 기본 제공 JDBC Repository 함수 사용시 대비, 기존 비즈니스 로직의 힙 메모리 사용량 2.5배 개선 및 불필요 DB 커넥션 풀 할당 평균 1개 이하로 개선  
  
| 개선 전/후 | avg | p(90) | p(95) | max | RPS(req / s) | JVM Heap Memory(MB) | JVM Heap Memory(%) | JVM Stop the world | DB 커넥션 풀 | App Total Memory(MB) | CPU Total |   
| --------- | --- | ----- | ----- | --- | ------------ | ------------------- | ------------------ | -------------- | ----------- | -------------------- | --------- |   
| 개선 전 | 1.6s | 2.75s | 3.33s | 12.87s | 123.596799 | 359.12 | 17.61 | 36.25 | 44.48 | 1002.95 | 1.28 |    
| 개선 후 | 17.2ms | 9.25ms | 72.25ms | 1.82s | 398.995026 | 147.97 | 7.41 | 3.68 | 0.62 | 596 | 0.55 |  
  
4. **Redis 알람 큐의 구조 개선으로 많은 양의 트래픽에서 발생 가능한 초기 spike 제거 및 스케줄러 작업시간 최적화**  
- Redis 알람 큐의 key 저장 구조를 개별 키 -> 통합 키로 개선하여, deq 시의 스케줄러 작업 시간 4배 개선 및 단편화율 1.2 수준 유지  
  
| 개선 전/후 | Redis 메모리 단편화율 | avg time(deq) |  
| --------- | ---------------------------- | ----------------- | -------------------- |  
| 개선 전(user 개별 키) | 1.15 | 763.95ms |  
| 개선 후(전역 통합 키) | 1.21 | 169.63ms |
