package ru.yandex.practicum.filmorate.sort;

public interface SortDirectorFilmsStrategy {
    String getSortSQL(int directorId);
}
