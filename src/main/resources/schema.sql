CREATE TABLE IF NOT EXISTS Users
(
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       login VARCHAR(255) NOT NULL,
                       name VARCHAR(255),
                       birthday DATE NOT NULL
);