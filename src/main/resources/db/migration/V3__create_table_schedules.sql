CREATE TABLE IF NOT EXISTS schedules
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(64) NOT NULL,
    location_id BIGINT,
    start_at TIMESTAMP(6) NOT NULL,
    end_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT schedule__fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT schedule__fk_location_id FOREIGN KEY (location_id) REFERENCES location (id) ON DELETE CASCADE
);

/*
 location_id : user가 실제로 약속이 있는 장소(일정 장소)
 */