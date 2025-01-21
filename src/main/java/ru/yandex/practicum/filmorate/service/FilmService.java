package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.EventStorage;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Director;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.sort.SortDirectorFilmsByDate;
import ru.yandex.practicum.filmorate.sort.SortDirectorFilmsByLikes;
import ru.yandex.practicum.filmorate.sort.SortDirectorFilmsStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exeption.ErrorMessages.DIRECTOR_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final Logger logger = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;
    private final DirectorDao directorDao;
    private static final Map<String, SortDirectorFilmsStrategy> SORT_DIRECTOR_FILMS_STRATEGIES = Map.of(
            "year", new SortDirectorFilmsByDate(),
            "likes", new SortDirectorFilmsByLikes()
    );

    private List<Film> getFilmsFullData(List<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Long, Set<Director>> filmDirectors = directorDao.loadFilmsDirectors(filmIds);

        films.forEach(film -> {
            film.setDirectors(new LinkedHashSet<>(filmDirectors.getOrDefault(film.getId(), Set.of())));
        });

        return films;
    }
  
    private final EventStorage eventStorage;

    public Film addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        userService.getUserOrThrow(userId);
        film.getLikes().add(userId);
        String sqlQuery = "INSERT INTO Likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        filmStorage.update(film);

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

    public List<Director> getDirectors() {
        return directorDao.findAllDirectors();
    }

    public Director findDirectorById(int id) {
        return directorDao.findDirectorById(id).orElseThrow(() -> new NotFoundObjectException(DIRECTOR_NOT_FOUND + id));
    }

    public LinkedHashSet<Director> findDirectorsForFilm(Long id) {
        return new LinkedHashSet<>(directorDao.findDirectorForFilm(id));
    }

    public Director createDirector(Director director) {
        return directorDao.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorDao.updateDirector(director);
    }

    public void deleteDirector(int id) {
        directorDao.deleteDirector(id);
    }

    public void updateDirectorsForFilm(Film film) {
        directorDao.updateDirectorOfFilm(film);
    }

    public ResponseEntity<Object> getFilmsByDirectorSorted(int directorId, String sortParam, FilmDtoMapper filmMapper) {
        try {
            List<Film> films;
            if (SORT_DIRECTOR_FILMS_STRATEGIES.containsKey(sortParam)) {
                films = getFilmsFullData(filmStorage.getFilmsByDirectorSorted(directorId,
                        SORT_DIRECTOR_FILMS_STRATEGIES.get(sortParam.toLowerCase())));
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid sortBy parameter: '" + sortParam +
                        "'. Allowed values - year, likes");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (films.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(films.stream()
                        .map((Film film) -> filmMapper.toDto(film))
                        .toList());
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }
}
