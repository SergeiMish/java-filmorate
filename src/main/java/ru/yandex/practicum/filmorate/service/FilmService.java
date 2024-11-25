package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final InMemoryFilmStorage inMemoryFilmStorage;

    public FilmService(InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public Film addLike(Long id, Long userId) {
        Film film = inMemoryFilmStorage.getFilmById(id);
        if (film == null) {
            throw new ValidationException("ID " + id + " не найден");
        }

        if (!film.getLikes().contains(userId)) {
            film.getLikes().add(userId);
            inMemoryFilmStorage.updateFilm(film);
        }

        return film;
    }

    public Film removeLike(Long id, Long userId) {
        Film film = inMemoryFilmStorage.getFilmById(id);
        if (film == null) {
            throw new ValidationException("ID " + id + " не найден");
        }

        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            inMemoryFilmStorage.updateFilm(film);
        }

        return film;
    }

    public List<Film> mostPopularFilms(int limit) {
        return inMemoryFilmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
