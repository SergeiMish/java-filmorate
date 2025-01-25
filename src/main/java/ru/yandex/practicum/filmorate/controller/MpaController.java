package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.mapper.MpaDtoMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final FilmService service;

    private final MpaDtoMapper mapper;

    @GetMapping
    public List<MpaDto> findAllMpa() {
        List<Mpa> ratings = service.getRatings();
        return ratings.stream().map(mapper::map).toList();
    }

    @GetMapping("/{id}")
    public MpaDto findMpa(@PathVariable int id) {
        Mpa rating = service.findMpaRatingById(id);
        return mapper.map(rating);
    }
}
