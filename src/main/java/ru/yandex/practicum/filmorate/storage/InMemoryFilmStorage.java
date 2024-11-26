package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    @NotNull
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate localDateMin = LocalDate.of(1895, 12, 28);

    public Film createFilm(Film film) {
        log.info("Attempting to create film: {}", film);

        // Проверка названия фильма
        if (film.getName() == null || film.getName().isEmpty()) {
            log.warn("Film name is empty");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        // Проверка описания фильма
        if (film.getDescription() == null || film.getDescription().isEmpty()) {
            log.warn("Film description is empty");
            throw new ValidationException("Описание фильма не может быть пустым");
        }

        // Проверка даты релиза
        if (film.getReleaseDate().isBefore(localDateMin) || film.getReleaseDate().isEqual(localDateMin)) {
            log.warn("Release date is before the minimum allowed date: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        // Проверка продолжительности фильма
        if (film.getDuration() <= 0) {
            log.warn("Film duration is not positive: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }

        // Установка ID и добавление фильма в хранилище
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Successfully created film with ID: {}", film.getId());
        return film;
    }

    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film deleteFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundObjectException("Фильм с таким ID не найден");
        }
        return films.remove(film.getId());
    }

    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundObjectException("Фильм с таким ID не найден");
        }
        if (film.getReleaseDate().isBefore(localDateMin)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        films.put(film.getId(), film);
        return film;
    }

    private long getNextId() {
        return films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}
