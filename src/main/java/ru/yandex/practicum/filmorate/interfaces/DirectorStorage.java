package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAll();

    Optional<Director> getById(int id);

    boolean contains(Integer directorId);

    Director create(Director director);

    Director update(Director director);

    void delete(int directorId);

    void updateDirectorsByFilm(Film film);

    List<Director> getDirectorsByFilm(int filmId);

    Map<Integer, Set<Director>> loadFilmsDirectors(List<Integer> filmIds);
}