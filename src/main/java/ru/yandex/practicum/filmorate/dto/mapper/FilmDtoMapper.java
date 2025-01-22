package ru.yandex.practicum.filmorate.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDtoMapper {

    public static Film map(CreateFilmDto filmDto) {
        return Film.builder()
                   .name(filmDto.getName())
                   .description(filmDto.getDescription())
                   .mpa(MpaDtoMapper.toModel(filmDto.getMpa()))
                   .releaseDate(filmDto.getReleaseDate())
                   .duration(filmDto.getDuration())
                   .genres(filmDto.getGenres() != null ? filmDto.getGenres().stream()
                                                                .map(GenreDtoMapper::toModel)
                                                                .collect(Collectors.toList()) : new ArrayList<>())
                   .directors(filmDto.getDirector() != null ? filmDto.getDirector().stream()
                                                                      .map(directorDto -> Director.builder()
                                                                                                  .id(directorDto.getId())
                                                                                                  .name(directorDto.getName())
                                                                                                  .build())
                                                                      .collect(Collectors.toList()) : new ArrayList<>())
                   .build();
    }

    public static FilmDto toDto(Film model) {
        return FilmDto.builder()
                      .id(model.getId())
                      .name(model.getName())
                      .description(model.getDescription())
                      .releaseDate(model.getReleaseDate())
                      .duration(model.getDuration())
                      .likes(model.getLikes() != null ? new HashSet<>(model.getLikes()) : new HashSet<>())
                      .genres(model.getGenres() != null ? model.getGenres().stream()
                                                               .map(GenreDtoMapper::toDto)
                                                               .collect(Collectors.toList()) : new ArrayList<>())
                      .mpa(model.getMpa())
                      .directors(model.getDirectors() != null ? model.getDirectors().stream()
                                                                     .map(DirectorDtoMapper::toDto)
                                                                     .collect(Collectors.toList()) : new ArrayList<>()) // Инициализация
                      .build();
    }

    public static Film toModel(FilmDto filmDto) {
        return Film.builder()
                   .name(filmDto.getName())
                   .description(filmDto.getDescription())
                   .releaseDate(filmDto.getReleaseDate())
                   .duration(filmDto.getDuration())
                   .mpa(filmDto.getMpa())
                   .genres(filmDto.getGenres() != null ? filmDto.getGenres().stream()
                                                                .map(GenreDtoMapper::toModel)
                                                                .collect(Collectors.toList()) : new ArrayList<>())
                   .directors(filmDto.getDirectors() != null ? filmDto.getDirectors().stream()
                                                                      .map(DirectorDtoMapper::toEntity)
                                                                      .collect(Collectors.toList()) : new ArrayList<>())
                   .build();
    }
}

