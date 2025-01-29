package ru.yandex.practicum.filmorate.model.enums;

import ru.yandex.practicum.filmorate.service.sorting.SortByLikes;
import ru.yandex.practicum.filmorate.service.sorting.SortByReleaseDate;
import ru.yandex.practicum.filmorate.service.sorting.SortStrategy;

public enum SortParam {
    YEAR(new SortByReleaseDate()),
    LIKES(new SortByLikes());

    private final SortStrategy sortStrategy;

    SortParam(SortStrategy sortStrategy) {
        this.sortStrategy = sortStrategy;
    }

    public SortStrategy getSortStrategy() {
        return sortStrategy;
    }
}