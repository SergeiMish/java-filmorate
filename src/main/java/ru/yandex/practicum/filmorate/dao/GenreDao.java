package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
public class GenreDao {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper = new GenreRowMapper();

    public GenreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT genre_id, name FROM Genres";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    public Genre getGenreById(Long id) {
        String sql = "SELECT genre_id, name FROM Genres WHERE genre_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, genreRowMapper);
    }
}