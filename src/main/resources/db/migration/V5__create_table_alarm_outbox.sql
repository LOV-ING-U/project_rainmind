CREATE TABLE IF NOT EXISTS alarm_outbox
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL
);

-- outbox 검색을 위한 index
CREATE INDEX __alarm_outbox_index ON alarm_outbox (status);