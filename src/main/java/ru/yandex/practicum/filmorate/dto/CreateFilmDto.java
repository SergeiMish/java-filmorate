package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.validator.AfterDate;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class CreateFilmDto {

    @NotBlank(message = "Film name can't be blank")
    @Size(max = 100, message = "Film name is too long")
    private String name;

    @NotBlank
    @Size(max = 200, message = "Description is too long")
    private String description;

    @NotNull
    private MpaDto mpa;

    @NotNull
    @AfterDate(value = "1895-12-28", message = "Release date should after 1st film birthday")
    private LocalDate releaseDate;

    @Positive(message = "Film duration should be positive")
    private int duration;

    private List<GenreDto> genres;

    private LinkedHashSet<DirectorDto> director;
}
