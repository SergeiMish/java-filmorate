package ru.yandex.practicum.filmorate.dto.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class MpaDtoMapper {

    public MpaDto map(Mpa mpa) {
        return MpaDto.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }

    public Mpa map(MpaDto mpa) {
        return Mpa.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }
}
