CREATE TABLE IF NOT EXISTS weather_forecast
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_code BIGINT NOT NULL,
    pop TINYINT NOT NULL,
    pty TINYINT NOT NULL,
    pcp TEXT NOT NULL,
    sky TINYINT NOT NULL,
    wsd DECIMAL(4, 1) NOT NULL,
    base_date_and_time TIMESTAMP(6) NOT NULL,
    fcst_date_and_time TIMESTAMP(6) NOT NULL,
    fetched_at TIMESTAMP(6) NOT NULL,

    UNIQUE KEY weather_forecast__uk_ (region_code, fcst_date_and_time),
    CONSTRAINT weather_forecast__fk_region_code FOREIGN KEY (region_code) REFERENCES location (id) ON DELETE CASCADE
);