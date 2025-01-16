package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class Review {

    private long reviewId;

    @NotNull(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва не может быть null")
    private boolean isPositive;

    private long userId;

    private long filmId;

    private int useful = 0;

    private Set<Long> usefulVotes = new HashSet<>();
    private Set<Long> notUsefulVotes = new HashSet<>();
}