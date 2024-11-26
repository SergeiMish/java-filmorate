package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

    Film createFilm(Film film);

    Film deleteFilm(Film film);

    Film updateFilm(Film film);

}
