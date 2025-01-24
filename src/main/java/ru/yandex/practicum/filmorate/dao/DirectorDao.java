package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
            String sqlQuery = "INSERT INTO directors (name) VALUES (?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
                stmt.setString(1, director.getName());
                return stmt;
            }, keyHolder);

            // Генерация id для нового директора
            director.setId(Optional.ofNullable(keyHolder.getKey()).map(Number::longValue)
                                   .orElseThrow(() -> new RuntimeException("Failed to generate director ID")));
            return director;
    }

    @Override
    public Director update(Director director) {
        validateDirectorExists(director.getId());
        String sqlQuery = "UPDATE directors SET name = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sqlQuery, director.getName(), director.getId());

        if (rows == 0) {
            throw new NotFoundObjectException("Director with id = " + director.getId() + " not found.");
        }

        return director;
    }

    @Override
    public boolean delete(Long id) {
        String sqlQuery = "DELETE FROM directors WHERE id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }


    @Override
    public List<Director> getDirectorsByFilm(Long filmId) {
        String sqlQuery = "SELECT d.id, d.name " +
                "FROM FilmsDirectors f_d " +
                "LEFT JOIN directors d " +
                "    ON f_d.director_id = d.id " +
                "WHERE film_id = ? " +
                "ORDER BY d.id ";


        return jdbcTemplate.query(sqlQuery, directorRowMapper, filmId).stream().toList();
    }

    @Override
    @Transactional
    public void updateDirectorsByFilm(Film film) {
        // Удаляем старые связи
        String deleteDirectorsQuery = "DELETE FROM FilmsDirectors WHERE film_id = ?";
        jdbcTemplate.update(deleteDirectorsQuery, film.getId());

        // Добавляем новые связи
        String insertDirectorsQuery = "INSERT INTO FilmsDirectors (film_id, director_id) VALUES (?, ?)";
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update(insertDirectorsQuery, film.getId(), director.getId());
            }
        }
    }

    @Override
    @Transactional
    public void deleteDirectorsByFilm(Film film) {
        String sqlQuery = "DELETE FROM FilmsDirectors WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    @Transactional
    public void addDirectorsByFilm(Film film) {
        String sqlQuery = "INSERT INTO FilmsDirectors (film_id, id) VALUES (?, ?)";
        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(sqlQuery, film.getId(), director.getId());
        }
    }

    @Override
    @Transactional
    public void addDirectorsByFilm(Film film, long filmId) {
        if (film.getDirectors() != null) {

            String sqlQuery = "INSERT INTO FilmsDirectors(film_id, director_id) VALUES (?, ?)";

            jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setLong(1, filmId);
                    preparedStatement.setLong(2, film.getDirectors().get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return film.getDirectors().size();
                }
            });
        }
    }

    public void validateDirectorExists(Long directorId) {
        Director director = getById(directorId);  // Получаем директора по ID
        if (director == null) {  // Если директора с таким ID нет
            throw new NotFoundObjectException("Директор с ID " + directorId + " не найден.");
        }
    }
}