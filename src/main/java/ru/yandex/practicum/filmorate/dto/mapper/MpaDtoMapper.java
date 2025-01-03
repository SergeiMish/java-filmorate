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

    public static Mpa toModel(MpaDto mpaDto) {
        return Mpa.builder()
                .id(mpaDto.getId())
                .name(mpaDto.getName())
                .build();
    }
}
