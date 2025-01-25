package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.mapper.FilmDtoMapper;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.film.searching.SearchByDirector;
import ru.yandex.practicum.filmorate.service.film.searching.SearchByDirectorAndTitle;
import ru.yandex.practicum.filmorate.service.film.searching.SearchByTitle;
import ru.yandex.practicum.filmorate.service.film.searching.SearchStrategy;
import ru.yandex.practicum.filmorate.service.sorting.SortDirectorFilmsByDate;
import ru.yandex.practicum.filmorate.service.sorting.SortDirectorFilmsByLikes;
import ru.yandex.practicum.filmorate.service.sorting.SortDirectorFilmsStrategy;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.model.enums.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;
    private final RatingStorage ratingStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    private static final Map<String, SortDirectorFilmsStrategy> SORT_DIRECTOR_FILMS_STRATEGIES = Map.of(
            "year", new SortDirectorFilmsByDate(),
            "likes", new SortDirectorFilmsByLikes()
    );

    private static final Map<Set<String>, SearchStrategy> SEARCH__FILMS_STRATEGIES = Map.of(
            Set.of("director"), new SearchByDirector(),
            Set.of("title"), new SearchByTitle(),
            Set.of("director", "title"), new SearchByDirectorAndTitle()
    );

    private List<Film> getFilmsFullData(List<Film> films) {
        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, Set<Genre>> filmGenres = genreStorage.loadFilmsGenres(filmIds);
        Map<Integer, Set<Director>> filmDirectors = directorStorage.loadFilmsDirectors(filmIds);

        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>(filmGenres.getOrDefault(film.getId(), Set.of())));
            film.setDirectors(new LinkedHashSet<>(filmDirectors.getOrDefault(film.getId(), Set.of())));
        });

        return films;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.findAllFilms();
        return getFilmsFullData(films);
    }

    public List<Genre> getGenres() {
        return genreStorage.findAllGenres();
    }

    public List<Mpa> getRatings() {
        return ratingStorage.findAllMpaRatings();
    }

    public List<Director> getDirectors() {
        return directorStorage.findAllDirectors();
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id).orElseThrow(() -> new NotFoundObjectException(FILM_NOT_FOUND + id));
    }

    public Genre findGenreById(int id) {
        return genreStorage.findGenreById(id).orElseThrow(() -> new NotFoundObjectException(RATING_NOT_FOUND + id));
    }

    public Mpa findMpaRatingById(int id) {
        return ratingStorage.findMpaRatingById(id).orElseThrow(() -> new NotFoundObjectException(GENRE_NOT_FOUND + id));
    }

    public Director findDirectorById(int id) {
        return directorStorage.findDirectorById(id).orElseThrow(() -> new NotFoundObjectException(DIRECTOR_NOT_FOUND + id));
    }

    public LinkedHashSet<Genre> findGenresForFilm(int id) {
        return new LinkedHashSet<>(genreStorage.findGenresForFilm(id));
    }

    public void updateGenresForFilm(Film film) {
        genreStorage.updateGenresOfFilm(film);
    }

    public LinkedHashSet<Director> findDirectorsForFilm(int id) {
        return new LinkedHashSet<>(directorStorage.findDirectorForFilm(id));
    }

    public void updateDirectorsForFilm(Film film) {
        directorStorage.updateDirectorOfFilm(film);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Integer id) {
        directorStorage.deleteDirector(id);
    }

    public Film create(Film film) {
        return filmStorage.add(film);
    }

    public void removeFilm(Integer id) {
        filmStorage.removeFilm(id);
    }

    public Film updateFilm(Film newFilm) {
        genreStorage.updateGenresOfFilm(newFilm);
        directorStorage.updateDirectorOfFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundObjectException(USER_NOT_FOUND + userId);
        }

        if (!filmStorage.containsFilm(filmId)) {
            throw new NotFoundObjectException(FILM_NOT_FOUND + filmId);
        }
        if (!filmStorage.checkLikesUserByFilmId(filmId, userId)) {
            filmStorage.addLike(filmId, userId);
        }
        feedStorage.addFeed(filmId, userId, LIKE, ADD);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundObjectException(USER_NOT_FOUND + userId);
        }

        if (!filmStorage.containsFilm(filmId)) {
            throw new NotFoundObjectException(FILM_NOT_FOUND + filmId);
        }

        filmStorage.removeLike(filmId, userId);
        feedStorage.addFeed(filmId, userId, LIKE, REMOVE);
    }

    public List<Film> getMostPopularFilms(int size) {
        return getFilmsFullData(filmStorage.getMostPopularFilms(size));
    }

    public List<Film> searchFilms(String query, Set<String> by) {
        List<Film> foundedFilms;
        if (by == null || by.isEmpty()) {
            throw new ValidationException("Передано некорректное число параметров");
        } else {
            if (SEARCH__FILMS_STRATEGIES.containsKey(by)) {
                foundedFilms = filmStorage.searchFilmsBy(query, SEARCH__FILMS_STRATEGIES.get(by));
            } else {
                throw new NotFoundObjectException("Неверно указан параметр поиска");
            }
        }
        return getFilmsFullData(foundedFilms);
    }

    public ResponseEntity<Object> getFilmsByDirectorSorted(int directorId, String sortParam, FilmDtoMapper filmMapper) {
        try {
            List<Film> films;
            if (SORT_DIRECTOR_FILMS_STRATEGIES.containsKey(sortParam)) {
                films = getFilmsFullData(filmStorage.getFilmsByDirectorSorted(directorId,
                        SORT_DIRECTOR_FILMS_STRATEGIES.get(sortParam.toLowerCase())));
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid sortBy parameter: '" + sortParam +
                        "'. Allowed values - year, likes");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (films.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(films.stream()
                        .map(filmMapper::map)
                        .toList());
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }

    public List<Film> getPopularFilmsSortedByGenreAndYear(Integer count, Integer genreId, Integer year) {
        return getFilmsFullData(filmStorage.getPopularFilmsSortedByGenreAndYear(count, genreId, year));
    }

    public List<Film> getPopularFilmsSortedByGenre(Integer count, Integer genreId) {
        return getFilmsFullData(filmStorage.getPopularFilmsSortedByGenre(count, genreId));
    }

    public List<Film> getPopularFilmsSortedByYear(Integer count, Integer year) {
        return getFilmsFullData(filmStorage.getPopularFilmsSortedByYear(count, year));
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return getFilmsFullData(filmStorage.getCommonFilms(userId, friendId));
    }

    public List<Film> getFilmRecommendationsForUser(int userId) {
        return getFilmsFullData(filmStorage.getFilmRecommendationsForUser(userId));
    }
}
