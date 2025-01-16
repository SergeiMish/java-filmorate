package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortParam;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exeption.ErrorMessages.DIRECTOR_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final Logger logger = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();
    private final DirectorDao directorStorage;

    public Film addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId);
        userService.getUserOrThrow(userId);
        film.getLikes().add(userId);
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        filmStorage.update(film);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        userService.getUserOrThrow(userId);

        Set<Long> likes = filmLikes.get(filmId);
        if (likes != null) {
            likes.remove(userId);
            if (likes.isEmpty()) {
                filmLikes.remove(filmId);
            }
            filmStorage.update(film);
            logger.info("Лайк удален пользователем {} от фильма {}", userId, filmId);
        }

        return film;
    }

    private Film getFilmOrThrow(Long filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + filmId + " не найден");
        }
        return film;
    }

    public List<Film> mostPopularFilms(int limit) {
        List<Film> popularFilms = filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(this::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        logger.info("Получены самые популярные фильмы, количество: {}", limit);
        return popularFilms;
    }

    private int getLikesCount(Film film) {
        return filmLikes.getOrDefault(film.getId(), Collections.emptySet()).size();
    }

    public List<Director> getDirectors() {
        return directorStorage.findAllDirectors();
    }

    public Director findDirectorById(int id) {
        return directorStorage.findDirectorById(id).orElseThrow(() -> new NotFoundObjectException(DIRECTOR_NOT_FOUND + id));
    }

    public LinkedHashSet<Director> findDirectorsForFilm(int id) {
        return new LinkedHashSet<>(directorStorage.findDirectorForFilm(id));
    }

    public void addDirectorsForFilm(Film film) {
        directorStorage.addDirectorOfFilm(film);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Integer id) {
        directorStorage.deleteDirector(id);
    }

    public Film updateFilm(Film newFilm) {
        directorStorage.addDirectorOfFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public List<Film> getFilmsByDirectorSorted(int directorId, FilmSortParam sortParam) {
        return filmStorage.getFilmsByDirectorSorted(directorId, sortParam);
    }
}
