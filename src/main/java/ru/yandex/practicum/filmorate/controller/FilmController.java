package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validator.ValidateFilm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.model.FilmSortParam.FILMS_BY_RELEASE_DATE;
import static ru.yandex.practicum.filmorate.model.FilmSortParam.POPULAR_FILMS_BY_LIKES;

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
        createdFilm.setDirector(film.getDirector());
        filmService.addDirectorsForFilm(createdFilm);
        return ResponseEntity.ok(FilmDtoMapper.toDto(createdFilm));
    }

    @GetMapping
    public Collection<FilmDto> getFilms() {
        List<Film> films = (List<Film>) filmStorage.getAll();
        for (Film film : films) {
            film.setDirector(filmService.findDirectorsForFilm((int) film.getId()));
        }
        return filmStorage.getAll().stream()
                .map(FilmDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilmById(@PathVariable Long id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundObjectException("Фильм с ID " + id + " не найден.");
        }
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
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(value = "limit", defaultValue = "10") @Positive int limit,
            @RequestParam(value = "genreId", required = false) Long genreId,
            @RequestParam(value = "year", required = false) Integer year) {
        List<Film> films = filmService.mostPopularFilms(count);
        for (Film film : films) {
            film.setDirector(filmService.findDirectorsForFilm((int) film.getId()));
        }
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

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Object> getFilmsByDirector(@PathVariable Integer directorId,
                                                     @RequestParam(name = "sortBy", required = false) String sortBy) {
        try {
            List<Film> films;
            switch (sortBy.toLowerCase()) {
                case "year":
                    films = filmService.getFilmsByDirectorSorted(directorId, FILMS_BY_RELEASE_DATE);
                    break;
                case "likes":
                    films = filmService.getFilmsByDirectorSorted(directorId, POPULAR_FILMS_BY_LIKES);
                    break;
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid sortBy parameter: '" + sortBy + "'. Allowed values - year, likes");
                    return ResponseEntity.badRequest().body(errorResponse);
            }

            for (Film film : films) {
                film.setDirector(filmService.findDirectorsForFilm((int) film.getId()));
            }
            return ResponseEntity.ok(films.stream()
                    .map(FilmDtoMapper::toDto)
                    .toList());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }
}
