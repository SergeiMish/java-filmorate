package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GenreDto {
    private Long id;
    private String name;
}

