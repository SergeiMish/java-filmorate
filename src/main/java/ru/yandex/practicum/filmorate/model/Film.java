package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class Film {

    private long id;

    @NotNull(message = "Название фильма не может быть пустым")
    @NotEmpty(message = "Название фильма не может быть пустым")
    private String name;

    @NotNull(message = "Описание фильма не может быть пустым")
    @NotEmpty(message = "Описание фильма не может быть пустым")
    @Size(max = 200, message = "Описание фильма не может быть больше 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть null")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    private Set<Long> likes = new HashSet<>();
    private List<Genre> genres;

    private Mpa mpa;
}