package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final InMemoryFilmStorage inMemoryFilmStorage;

    public FilmService(InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public void addLike (Long id, Long likeId){
        Film film = inMemoryFilmStorage.getFilmById(id);
        Film filmLike = inMemoryFilmStorage.getFilmById(likeId);
        if (film != null && filmLike != null) {
            film.getLikes().add(likeId);
            filmLike.getLikes().add(id);
            inMemoryFilmStorage.updateFilm(film);
            inMemoryFilmStorage.updateFilm(filmLike);
        }
    }

    public void removeLike (Long id, Long likeId){
        Film film = inMemoryFilmStorage.getFilmById(id);
        Film filmLike = inMemoryFilmStorage.getFilmById(likeId);
        if (film != null && filmLike != null) {
            film.getLikes().remove(likeId);
            filmLike.getLikes().remove(id);
            inMemoryFilmStorage.updateFilm(film);
            inMemoryFilmStorage.updateFilm(filmLike);
        }
    }

    public List<Film> mostPopularFilms(int limit) {
        return inMemoryFilmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
