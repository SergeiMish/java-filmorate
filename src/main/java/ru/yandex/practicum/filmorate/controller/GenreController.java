package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.mapper.GenreDtoMapper;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreDao genreDao;

    @GetMapping
    public ResponseEntity<List<GenreDto>> getAllGenres() {
        List<GenreDto> genres = genreDao.getAllGenres().stream()
                .map(GenreDtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenreById(@PathVariable Long id) {
        Genre genre = genreDao.getGenreById(id);
        return ResponseEntity.ok(GenreDtoMapper.toDto(genre));
    }
}