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

INSERT INTO location(region_name, latitude, longitude, nx, ny, created_at, updated_at) VALUES ('seoul sinlim', 037.487426, 126.927075, 59, 125, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));