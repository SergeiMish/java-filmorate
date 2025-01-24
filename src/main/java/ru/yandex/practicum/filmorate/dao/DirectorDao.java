package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.DirectorStorage;
import ru.yandex.practicum.filmorate.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDao implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    public List<Director> getAll() {
        String sqlQuery = "SELECT id, name FROM directors";
        return jdbcTemplate.query(sqlQuery, directorRowMapper);
    }

    public Director getById(Long id) {
        String sqlQuery = "SELECT id, name FROM directors WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, directorRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundObjectException("Director with id = " + id + " not found.");
        }
    }

    @Override
    @Transactional
    public Director create(Director director) {
        try {
            String sqlQuery = "INSERT INTO directors (name) VALUES (?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
                stmt.setString(1, director.getName());
                return stmt;
            }, keyHolder);

            // Генерация id для нового директора
            director.setId(Optional.ofNullable(keyHolder.getKey()).map(Number::longValue)
                                   .orElseThrow(() -> new RuntimeException("Failed to generate director ID")));
            return director;
        } catch (Exception e) {
            log.error("Error while creating director", e);
            throw new RuntimeException("Error while creating director", e);
        }
    }

    public Director update(Director director) {
        String sqlQuery = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        int rows = jdbcTemplate.update(sqlQuery, director.getName(), director.getId());

        if (rows == 0) {
            throw new NotFoundObjectException("Director with id = " + director.getId() + " not found.");
        }

        return director;
    }

    public boolean delete(Long id) {
        String sqlQuery = "DELETE FROM directors WHERE id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }


    @Override
    public List<Director> getDirectorsByFilm(Long filmId) {
        String sqlQuery = "SELECT d.id, d.name FROM film_director f_d INNER JOIN directors d ON f_d.director_id = d.id WHERE f_d.film_id = ? ORDER BY d.id";
        return jdbcTemplate.query(sqlQuery, directorRowMapper::mapRow, filmId);
    }

    @Override
    @Transactional
    public void updateDirectorsByFilm(Film film) {
        // Удаление старых связей
        String deleteQuery = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(deleteQuery, film.getId());

        // Добавление новых связей
        String insertQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(insertQuery, film.getId(), director.getId());
        }
    }

    @Override
    @Transactional
    public void deleteDirectorsByFilm(Film film) {
        String sqlQuery = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    @Transactional
    public void addDirectorsByFilm(Film film) {
        String sqlQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(sqlQuery, film.getId(), director.getId());
        }
    }

    @Override
    @Transactional
    public void addDirectorsByFilm(Film film, long filmId) {
        String sqlQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(sqlQuery, filmId, director.getId());
        }
    }

    public void validateDirectorExists(Long directorId) {
        Director director = getById(directorId);  // Получаем директора по ID
        if (director == null) {  // Если директора с таким ID нет
            throw new NotFoundObjectException("Директор с ID " + directorId + " не найден.");
        }
    }
}