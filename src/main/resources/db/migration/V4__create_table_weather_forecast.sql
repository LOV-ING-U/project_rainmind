-- 기상청 단기예보를 격자(nx, ny)와 예보 시각 단위로 저장한다.
CREATE TABLE IF NOT EXISTS weather_forecast
(
    nx         INT          NOT NULL,
    ny         INT          NOT NULL,
    fcst_at    TIMESTAMP(6) NOT NULL,
    sky        TINYINT      NULL,
    pty        TINYINT      NULL,
    pop        TINYINT      NULL,
    tmp        DECIMAL(4, 1) NULL,
    base_at    TIMESTAMP(6) NOT NULL,
    fetched_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (nx, ny, fcst_at)
);
