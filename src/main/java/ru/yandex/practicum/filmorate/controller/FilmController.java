package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.SortParam;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private static final int FIRST_FILM_BIRTHDAY = 1895;

    private final FilmService service;

    private final FilmDtoMapper filmMapper;

    @GetMapping
    public List<FilmDto> findAll() {
        List<Film> films = service.getFilms();
        return films.stream()
                .map(filmMapper::map)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        Film createdFilm = service.create(filmDto);
        return filmMapper.map(createdFilm);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable Integer filmId) {
        service.removeFilm(filmId);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto filmDto) {
        Film updatedFilm = service.updateFilm(filmDto);
        return filmMapper.map(updatedFilm);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable int id) {
        Film film = service.findFilmById(id);
        return filmMapper.map(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        service.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") @Positive Integer count,
                                         @Positive @RequestParam(required = false) Integer genreId,
                                         @Min(value = FIRST_FILM_BIRTHDAY) @RequestParam(required = false) Integer year) {
        return service.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Object> getFilmsByDirector(@PathVariable Integer directorId,
                                                     @RequestParam(name = "sortBy", required = false, defaultValue = "YEAR") String sortBy) {
        try {
            SortParam sortParam = SortParam.valueOf(sortBy.toUpperCase());
            return service.getFilmsByDirectorSorted(directorId, sortParam, filmMapper);
        } catch (IllegalArgumentException e) {
            String error = "Invalid sortBy parameter: '" + sortBy + "'. Allowed values - YEAR, LIKES";
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", error));
        }
    }

    @GetMapping("/common")
    public ResponseEntity<Object> getCommonFilms(@RequestParam("userId") int userId,
                                                 @RequestParam("friendId") int friendId) {
        List<FilmDto> filmDto = service.getCommonFilms(userId, friendId);
        return ResponseEntity.ok(filmDto);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam() String query, @RequestParam() Set<String> by) {
        return service.searchFilms(query, by);
    }
}
