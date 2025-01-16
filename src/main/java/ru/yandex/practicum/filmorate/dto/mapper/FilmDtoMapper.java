package ru.yandex.practicum.filmorate.dto.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmDtoMapper {

    private static final DirectorMapper directorMapper = new DirectorMapper(); //стоит обратить внимание

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
                .director(directorMapper.mapToDirectorDtoList(model.getDirector()))
                .build();
    }

    public static Film toModel(FilmDto filmDto) {
        return Film.builder()
                .id(filmDto.getId())
                .name(filmDto.getName())
                .description(filmDto.getDescription())
                .releaseDate(filmDto.getReleaseDate())
                .duration(filmDto.getDuration())
                .likes(filmDto.getLikes() != null ? new HashSet<>(filmDto.getLikes()) : new HashSet<>())
                .genres(filmDto.getGenres() != null ? filmDto.getGenres().stream()
                        .map(GenreDtoMapper::toModel)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .mpa(filmDto.getMpa())
                .director(directorMapper.mapToDirectorList(filmDto.getDirector()))
                .build();
    }
}
