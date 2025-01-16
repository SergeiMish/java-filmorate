package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validator.ValidateFilm;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final ValidateFilm filmValidator;

    @PostMapping
    public ResponseEntity<FilmDto> postFilm(@RequestBody @Valid FilmDto filmDto) {
        log.info("Received request to create film: {}", filmDto);
        Film film = FilmDtoMapper.toModel(filmDto);
        filmValidator.validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Film created successfully: {}", createdFilm);
        return ResponseEntity.ok(FilmDtoMapper.toDto(createdFilm));
    }

    @GetMapping
    public Collection<FilmDto> getFilms() {
        return filmStorage.getAll().stream()
                .map(FilmDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilmById(@PathVariable Long id) {
        Film film = filmStorage.getById(id);
        return ResponseEntity.ok(FilmDtoMapper.toDto(film));
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(value = "limit", defaultValue = "10") @Positive int limit,
            @RequestParam(value = "genreId", required = false) Long genreId,
            @RequestParam(value = "year", required = false) Integer year) {
        return filmService.mostPopularFilms(limit, genreId, year).stream()
                .map(FilmDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDto> addLike(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.addLike(id, userId);
        return ResponseEntity.ok(FilmDtoMapper.toDto(film));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDto> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        Film film = filmService.removeLike(id, userId);
        return ResponseEntity.ok(FilmDtoMapper.toDto(film));
    }

    @PutMapping
    public ResponseEntity<FilmDto> putFilm(@Valid @RequestBody FilmDto filmDto) {
        Film film = FilmDtoMapper.toModel(filmDto);
        filmValidator.validateFilm(film);
        Film updatedFilm = filmStorage.update(film);
        return ResponseEntity.ok(FilmDtoMapper.toDto(updatedFilm));
    }
}
