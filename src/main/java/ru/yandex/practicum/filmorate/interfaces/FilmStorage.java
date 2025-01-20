package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.sort.SortDirectorFilmsStrategy;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    boolean delete(Long id);

    Film update(Film film);

    Film getById(Long id);

    Collection<Film> getAll();

    List<Film> getFilmsByDirectorSorted(int directorId, SortDirectorFilmsStrategy sortDirectorFilmsStrategy);

    List<Film> getFilmsByUserId(Long userId);

}
