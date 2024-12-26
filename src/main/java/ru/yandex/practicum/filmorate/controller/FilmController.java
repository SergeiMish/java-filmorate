package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validator.ValidateFilm;

import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final ValidateFilm filmValidator;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService, ValidateFilm filmValidator) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.filmValidator = filmValidator;
    }


    @PostMapping
    public Film postFilm(@RequestBody @Valid Film film) {
        filmValidator.validateFilm(film);
        return filmStorage.create(film);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmStorage.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmStorage.getById(id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(value = "count", defaultValue = "10") @Positive int count) {
        return filmService.mostPopularFilms(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.removeLike(id, userId);
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
        filmValidator.validateFilm(film);
        return filmStorage.update(film);
    }
}
