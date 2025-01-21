package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    List<Director> findAllDirectors();

    Optional<Director> findDirectorById(int id);

    boolean containsDirector(Integer directorId);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int directorId);

    List<Director> findDirectorForFilm(Long filmId);

    void updateDirectorOfFilm(Film film);

    Map<Long, Set<Director>> loadFilmsDirectors(List<Long> filmIds);
}
