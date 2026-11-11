-- Redis ZSET 에 아직 등록되지 않은 알림만 담는다. 등록에 성공하면 행을 삭제한다.
CREATE TABLE IF NOT EXISTS alarm_outbox
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT       NOT NULL,
    alarm_at    TIMESTAMP(6) NOT NULL
);
