package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final InMemoryFilmStorage inMemoryFilmStorage;

    public FilmController(InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }


    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) {
        return inMemoryFilmStorage.createFilm(film);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return inMemoryFilmStorage.getFilm();
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
        return inMemoryFilmStorage.updateFilm(film);
    }

}
