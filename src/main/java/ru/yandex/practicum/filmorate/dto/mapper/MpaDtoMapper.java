package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

public class MpaDtoMapper {

    public static MpaDto toDto(Mpa model) {
        return MpaDto.builder()
                .id(model.getId())
                .name(model.getName())
                .build();
    }

    public static Mpa toModel(MpaDto dto) {
        return Mpa.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
