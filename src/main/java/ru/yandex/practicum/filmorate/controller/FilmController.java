package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    @NotNull
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate localDateMin = LocalDate.of(1895, 12, 28);


    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(localDateMin) || film.getReleaseDate().isEqual(localDateMin)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;

    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
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
