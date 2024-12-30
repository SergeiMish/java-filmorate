package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashSet;

public class FilmDtoMapper {

    public static FilmDto toDto(Film model) {
        return FilmDto.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .releaseDate(model.getReleaseDate())
                .duration(model.getDuration())
                .likes(new HashSet<>(model.getLikes()))
                .genres(new ArrayList<>(model.getGenres()))
                .mpaRating(model.getMpaRating())
                .build();
    }

    public static Film toModel(FilmDto filmDto) {
        return Film.builder()
                .id(filmDto.getId())
                .name(filmDto.getName())
                .description(filmDto.getDescription())
                .releaseDate(filmDto.getReleaseDate())
                .duration(filmDto.getDuration())
                .likes(new HashSet<>(filmDto.getLikes()))
                .genres(new ArrayList<>(filmDto.getGenres()))
                .mpaRating(filmDto.getMpaRating())
                .build();
    }
}
