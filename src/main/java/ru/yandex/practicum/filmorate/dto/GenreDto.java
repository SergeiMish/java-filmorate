package ru.yandex.practicum.filmorate.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class GenreDto {
    private Long id;
    private String name;
}

