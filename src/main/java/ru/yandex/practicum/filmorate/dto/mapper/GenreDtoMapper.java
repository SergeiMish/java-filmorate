package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

public class GenreDtoMapper {
    public static GenreDto toDto(Genre model) {
        return GenreDto.builder()
                .id(model.getId())
                .name(model.getName())
                .build();
    }

    public static Genre toModel(GenreDto genreDto) {
        return Genre.builder()
                .id(genreDto.getId())
                .name(genreDto.getName())
                .build();
    }
}
