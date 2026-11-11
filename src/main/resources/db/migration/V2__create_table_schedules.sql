CREATE TABLE IF NOT EXISTS schedules
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(64)  NOT NULL,
    start_at    TIMESTAMP(6) NOT NULL,
    region_name VARCHAR(128) NOT NULL,
    nx          INT          NOT NULL,
    ny          INT          NOT NULL,
    notified_at TIMESTAMP(6) NULL,

    CONSTRAINT schedules__fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 시간순 보조 인덱스는 두지 않는다(쓰기 집중 테이블 가정). 알림 시각 정렬은 Redis ZSET 이 담당한다.
