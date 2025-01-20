package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class ReviewDto {

    private Long reviewId;

    @NotNull(message = "Текст отзыва не может быть пустым")
    @Size(max = 1000, message = "Текст отзыва не может быть длиннее 1000 символов")
    private String content;

    @NotNull(message = "Тип отзыва не может быть null")
    private Boolean isPositive;

    private Long userId;

    private Long filmId;

    private int useful;
}