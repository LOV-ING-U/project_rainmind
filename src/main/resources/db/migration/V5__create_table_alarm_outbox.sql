CREATE TABLE IF NOT EXISTS alarm_outbox
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL
);