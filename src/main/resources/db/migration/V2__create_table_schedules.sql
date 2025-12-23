CREATE TABLE IF NOT EXISTS schedules
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(64) NOT NULL,
    location VARCHAR(64) NOT NULL,
    start_at TIMESTAMP(6) NOT NULL,
    end_at TIMESTAMP(6) NOT NULL,
    region_name VARCHAR(128) NOT NULL,

    CONSTRAINT schedule__fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT schedule__fk_region_name FOREIGN KEY (region_name) REFERENCES location (region_name) ON DELETE CASCADE
);