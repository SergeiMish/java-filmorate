package ru.yandex.practicum.filmorate.dto.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

@Component
public class DirectorDtoMapper {

    public static DirectorDto toDto(Director model) {
        return DirectorDto.builder()
                          .id(model.getId())
                          .name(model.getName())
                          .build();
    }

    public static Director toEntity(DirectorDto dto) {
        return Director.builder()
                       .id(dto.getId())
                       .name(dto.getName())
                       .build();
    }
}