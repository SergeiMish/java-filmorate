package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class FilmDto {
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
    @Builder.Default
    private Set<Long> likes = new HashSet<>();
    @Builder.Default
    private List<String> genres = new ArrayList<>();
    private MpaRating mpaRating;
}
