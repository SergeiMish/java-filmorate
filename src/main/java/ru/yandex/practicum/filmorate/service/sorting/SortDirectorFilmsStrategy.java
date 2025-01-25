package ru.yandex.practicum.filmorate.service.sorting;

public interface SortDirectorFilmsStrategy {
    String getSortSQL(int directorId);
}
