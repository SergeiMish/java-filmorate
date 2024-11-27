package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film createFilm(Film film);

    Film deleteFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(Long id);

    Collection<Film> getAllFilms();

}
