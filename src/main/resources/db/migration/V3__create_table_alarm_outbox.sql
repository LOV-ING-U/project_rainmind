-- Redis ZSET 에 아직 등록되지 않은 알림만 담는다. 등록에 성공하면 행을 삭제한다.
CREATE TABLE IF NOT EXISTS alarm_outbox
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT       NOT NULL,
    alarm_at    TIMESTAMP(6) NOT NULL,

    -- 일정 삭제 시 schedule_id 로 outbox 를 지운다. 인덱스가 없으면 전체 스캔하며
    -- 모든 행과 supremum 에 next-key 락이 걸려, 그동안 다른 사용자의 일정 생성(INSERT)이 막힌다.
    INDEX idx_alarm_outbox_schedule_id (schedule_id)
);
