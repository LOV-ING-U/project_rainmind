CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname      VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    CONSTRAINT users__uk_nickname UNIQUE (nickname)
);
