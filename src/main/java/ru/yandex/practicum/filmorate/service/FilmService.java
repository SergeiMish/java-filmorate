package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.DirectorStorage;
import ru.yandex.practicum.filmorate.interfaces.EventStorage;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final Logger logger = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;
    private final DirectorStorage directorStorage;
    private final EventStorage eventStorage;

    private List<Film> getFilmsFullData(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        return films;
    }

    public Film addLike(Long filmId, Long userId) {
        log.info("Попытка пользователя {} добавить лайк фильму {}", userId, filmId);

        log.info("Попытка пользователя {} добавить лайк фильму {}", userId, filmId);

        String checkLikeSql = "SELECT COUNT(*) FROM Likes WHERE film_id = ? AND user_id = ?";
        Long likeCount = jdbcTemplate.queryForObject(checkLikeSql, Long.class, filmId, userId);

        if (likeCount != null && likeCount > 0) {
            return filmStorage.getById(filmId);
        }

        Film film = filmStorage.getById(filmId);
        userService.getUserOrThrow(userId);

        film.getLikes().add(userId);

        String sqlQuery = "INSERT INTO Likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);

        eventStorage.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType("LIKE")
                .operation("ADD")
                .entityId(filmId)
                .build());

        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);

        String checkLikeSql = "SELECT COUNT(*) FROM Likes WHERE film_id = ? AND user_id = ?";
        Long likeCount = jdbcTemplate.queryForObject(checkLikeSql, Long.class, filmId, userId);

        if (likeCount == null || likeCount == 0) {
            logger.info("Лайк от пользователя {} для фильма {} не найден.", userId, filmId);
            return film;
        }

        String sqlQuery = "DELETE FROM Likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        film.getLikes().remove(userId);
        filmStorage.update(film);

        logger.info("Лайк удален пользователем {} от фильма {}", userId, filmId);

        eventStorage.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType("LIKE")
                .operation("REMOVE")
                .entityId(filmId)
                .build());

        return film;
    }

    private Film getFilmOrThrow(Long filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + filmId + " не найден");
        }
        return film;
    }

    public List<Film> mostPopularFilms(int limit, Long genreId, Integer year) {
        logger.info("Получение самых популярных фильмов. Параметры: limit={}, genreId={}, year={}",
                limit, genreId, year);
        return filmStorage.getAll().stream()
                .filter(film -> genreId == null || film.getGenres().stream()
                        .anyMatch(genre -> genre.getId().equals(genreId)))
                .filter(film -> year == null || film.getReleaseDate().getYear() == year)
                .sorted(Comparator.comparingInt(this::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        logger.info("Fetching common films for userId={} and friendId={}", userId, friendId);
        List<Film> userFilms = filmStorage.getFilmsByUserId(userId);
        List<Film> friendFilms = filmStorage.getFilmsByUserId(friendId);

        return userFilms.stream()
                .filter(friendFilms::contains)
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    private int getLikesCount(Film film) {
        String sqlQuery = "SELECT COUNT(*) FROM Likes WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, film.getId());
        return count != null ? count : 0;
    }

    public List<Film> getFilmsByDirectorAndOrByTitle(String query, String by) {
        return filmStorage.getFilmsByDirectorAndOrByTitle(query, by);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorStorage.getById(directorId);
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

}
