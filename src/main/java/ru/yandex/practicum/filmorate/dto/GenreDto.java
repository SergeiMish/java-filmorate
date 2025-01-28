package ru.yandex.practicum.filmorate.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class GenreDto {

    private int id;

    private String name;
}
