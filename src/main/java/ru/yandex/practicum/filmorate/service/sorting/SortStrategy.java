package ru.yandex.practicum.filmorate.service.sorting;

public interface SortStrategy {
    String getSortSQL(int directorId);
}