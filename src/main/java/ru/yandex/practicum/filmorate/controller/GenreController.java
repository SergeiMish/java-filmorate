package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreDao genreDao;

    @Autowired
    public GenreController(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        List<Genre> genres = genreDao.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable Long id) {
        try {
            Genre genre = genreDao.getGenreById(id);
            return ResponseEntity.ok(genre);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}