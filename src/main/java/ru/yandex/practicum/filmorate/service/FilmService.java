package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final InMemoryUserStorage inMemoryUserStorage;

    public FilmService(InMemoryFilmStorage inMemoryFilmStorage, InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public Film addLike(Long filmId, Long userId) {
        // Проверка существования фильма
        Film film = inMemoryFilmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + filmId + " не найден");
        }

        User user = inMemoryUserStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundObjectException("Пользователь с ID " + userId + " не найден");
        }

        if (!film.getLikes().contains(userId)) {
            film.getLikes().add(userId);
            inMemoryFilmStorage.updateFilm(film);
        }

        return film;
    }

    public Film removeLike(Long filmId, Long userId) {

        Film film = inMemoryFilmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + filmId + " не найден");
        }

        User user = inMemoryUserStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundObjectException("Пользователь с ID " + userId + " не найден");
        }

        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            inMemoryFilmStorage.updateFilm(film);
        }

        return film;
    }

    public List<Film> mostPopularFilms(int limit) {
        if (limit <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
        return inMemoryFilmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
