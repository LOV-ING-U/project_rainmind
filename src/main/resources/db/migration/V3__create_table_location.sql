CREATE TABLE IF NOT EXISTS location
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region_name VARCHAR(128) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,
    nx INT NOT NULL,
    ny INT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    UNIQUE KEY location__uk_ (region_name)
);