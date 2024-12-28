CREATE TABLE IF NOT EXISTS Users
(
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       login VARCHAR(255) NOT NULL,
                       name VARCHAR(255),
                       birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS Friendships (
                             user_id BIGINT,
                             friend_id BIGINT,
                             status VARCHAR(20) NOT NULL,
                             PRIMARY KEY (user_id, friend_id),
                             FOREIGN KEY (user_id) REFERENCES Users(user_id),
                             FOREIGN KEY (friend_id) REFERENCES Users(user_id)
);

CREATE TABLE IF NOT EXISTS Films (
                       film_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(255) NOT NULL,
                       description VARCHAR(200) NOT NULL,
                       release_date DATE NOT NULL,
                       duration INT NOT NULL,
                       mpa_rating VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS MpaRatings (
                                          mpa_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Genres (
                        genre_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS FilmGenres (
                            film_id BIGINT,
                            genre_id BIGINT,
                            PRIMARY KEY (film_id, genre_id),
                            FOREIGN KEY (film_id) REFERENCES Films(film_id),
                            FOREIGN KEY (genre_id) REFERENCES Genres(genre_id)
);

CREATE TABLE IF NOT EXISTS Likes (
                                     film_id BIGINT,
                                     user_id BIGINT,
                                     PRIMARY KEY (film_id, user_id),
                                     FOREIGN KEY (film_id) REFERENCES Films(film_id),
                                     FOREIGN KEY (user_id) REFERENCES Users(user_id)
);