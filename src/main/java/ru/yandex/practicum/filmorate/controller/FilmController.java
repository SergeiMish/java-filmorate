package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validator.ValidateFilm;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final ValidateFilm filmValidator;
    private final FilmDtoMapper filmDtoMapper;
    private final DirectorDao directorDao;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto postFilm(@RequestBody @Valid CreateFilmDto filmDto) {
        log.info("Received request to create film: {}", filmDto);
        Film film = FilmDtoMapper.map(filmDto);
        filmValidator.validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Film created successfully: {}", createdFilm);
        filmService.updateDirectorsForFilm(createdFilm);
        createdFilm.setDirectors(film.getDirectors());
        return FilmDtoMapper.toDto(createdFilm);
    }

    @GetMapping
    public Collection<FilmDto> getFilms() {
        List<Film> films = (List<Film>) filmStorage.getAll();
        return filmStorage.getAll().stream()
                .map(model -> filmDtoMapper.toDto(model))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilmById(@PathVariable Long id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + id + " не найден.");
        }
        film.setDirectors(filmService.findDirectorsForFilm(id));
        FilmDto filmDto = FilmDtoMapper.toDto(film);
        return ResponseEntity.ok(filmDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + id + " не найден.");
        }

        boolean isDeleted = filmStorage.delete(id);
        return isDeleted ? ResponseEntity.noContent().build() :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") @Positive int limit,
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
        directorDao.updateDirectorOfFilm(film);
        Film updatedFilm = filmStorage.update(film);
        filmService.updateDirectorsForFilm(updatedFilm);
        updatedFilm.setDirectors(film.getDirectors());
        return ResponseEntity.ok(FilmDtoMapper.toDto(updatedFilm));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Object> getFilmsByDirector(@PathVariable Integer directorId,
                                                     @RequestParam(name = "sortBy", required = false) String sortBy) {
        return filmService.getFilmsByDirectorSorted(directorId, sortBy, filmDtoMapper);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        log.info("Received request for common films of user {} and friend {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId).stream()
                .map(FilmDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam() String query, @RequestParam() Set<String> by) {
        return filmService.searchFilms(query, by);
    }
}
