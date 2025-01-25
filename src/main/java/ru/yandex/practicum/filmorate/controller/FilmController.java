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
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.dto.mapper.MpaDtoMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

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

    private final MpaDtoMapper ratingMapper;

    @GetMapping
    public List<FilmDto> findAll() {
        List<Film> films = service.getFilms();
        return films.stream().map(filmMapper::map).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody CreateFilmDto filmDto) {
        Film toCreate = filmMapper.map(filmDto);
        Film createdFilm = service.create(toCreate);
        service.updateGenresForFilm(createdFilm);
        service.updateDirectorsForFilm(createdFilm);
        createdFilm.setGenres(toCreate.getGenres());
        createdFilm.setDirectors(toCreate.getDirectors());
        return filmMapper.map(createdFilm);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable Integer filmId) {
        service.removeFilm(filmId);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        Film updatedFilm = service.updateFilm(film);
        service.updateGenresForFilm(updatedFilm);
        service.updateDirectorsForFilm(updatedFilm);
        updatedFilm.setGenres(film.getGenres());
        updatedFilm.setDirectors(film.getDirectors());
        return filmMapper.map(updatedFilm);
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable int id) {
        Film film = service.findFilmById(id);
        film.setGenres(service.findGenresForFilm(id));
        film.setDirectors(service.findDirectorsForFilm(id));
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
        List<Film> films;
        if (genreId == null && year == null) {
            films = service.getMostPopularFilms(count);
        } else if (genreId != null && year == null) {
            films = service.getPopularFilmsSortedByGenre(count, genreId);
        } else if (genreId != null) {
            films = service.getPopularFilmsSortedByGenreAndYear(count, genreId, year);
        } else {
            films = service.getPopularFilmsSortedByYear(count, year);
        }
        return films.stream()
                .map(filmMapper::map)
                .toList();
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Object> getFilmsByDirector(@PathVariable Integer directorId,
                                                     @RequestParam(name = "sortBy", required = false) String sortBy) {
        return service.getFilmsByDirectorSorted(directorId, sortBy, filmMapper);
    }

    @GetMapping("/common")
    public ResponseEntity<Object> getCommonFilms(@RequestParam("userId") int userId,
                                                 @RequestParam("friendId") int friendId) {
        try {
            List<Film> films = service.getCommonFilms(userId, friendId);

            return ResponseEntity.ok(films.stream()
                    .map(filmMapper::map)
                    .toList());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam() String query, @RequestParam() Set<String> by) {
        return service.searchFilms(query, by);
    }
}
