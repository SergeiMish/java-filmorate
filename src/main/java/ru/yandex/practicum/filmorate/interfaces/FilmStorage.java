package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    boolean delete(Long id);

    Film update(Film film);

    Film getById(Long id);

    Collection<Film> getAll();

    List<Film> getFilmsByUserId(Long userId);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);

    List<Film> getFilmsByDirectorAndOrByTitle(String query, String by);

}
