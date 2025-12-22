CREATE TABLE IF NOT EXISTS weather_forecast
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_name VARCHAR(128) NOT NULL,
    POP BIGINT NOT NULL,
    PTY BIGINT NOT NULL,
    PCP BIGINT NOT NULL,
    SKY BIGINT NOT NULL,
    WSD DECIMAL(3, 1) NOT NULL,
    base_date_and_time TIMESTAMP(6) NOT NULL,
    fcst_date_and_time TIMESTAMP(6) NOT NULL,
    fetched_at TIMESTAMP(6) NOT NULL,

    UNIQUE KEY weather_forecast__uk_ (region_name, fcst_date_and_time),
    CONSTRAINT weather_forecast__fk_region_name FOREIGN KEY (region_name) REFERENCES location (region_name) ON DELETE CASCADE
);