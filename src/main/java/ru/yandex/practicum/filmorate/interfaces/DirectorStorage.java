package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface DirectorStorage {

    Collection<Director> getAll();

    Director getById(Long id);

    Director update(Director director);

    Director create(Director director);

    boolean delete(Long id);

    List<Director> getDirectorsByFilm(Long filmId);

    void updateDirectorsByFilm(Film film);

    void deleteDirectorsByFilm(Film film);

    void addDirectorsByFilm(Film film);

    void addDirectorsByFilm(Film film, long filmId);
}