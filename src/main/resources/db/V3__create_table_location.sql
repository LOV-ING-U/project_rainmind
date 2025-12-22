CREATE TABLE IF NOT EXISTS location
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_name VARCHAR(128) NOT NULL,
    latitude DECIMAL(5, 2) NOT NULL,
    longitude DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);