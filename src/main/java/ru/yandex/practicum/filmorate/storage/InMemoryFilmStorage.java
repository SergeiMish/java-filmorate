package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    @NotNull
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate localDateMin = LocalDate.of(1895, 12, 28);

    @Override
    public Film createFilm(Film film) {
        if (film.getReleaseDate().isBefore(localDateMin) || film.getReleaseDate().isEqual(localDateMin)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;

    }

    public Collection<Film> getFilm() {
        return films.values();
    }

    @Override
    public Film deleteFilm(Film film) {
        return null;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Фильм с таким ID не найден");
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
