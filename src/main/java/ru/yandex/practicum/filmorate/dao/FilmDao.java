package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;


@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmDao implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film create(Film film) {
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_rating) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.debug("Executing SQL: {}", sqlQuery);
        log.debug("With parameters: name={}, description={}, releaseDate={}, duration={}, mpaRating={}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpaRating());

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
            stmt.setInt(4, film.getDuration());
            stmt.setString(5, String.valueOf(film.getMpaRating()));
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        log.info("Film created with ID: {}", film.getId());

        return film;
    }

    @Override
    public boolean delete(Long id) {
        String sqlQuery = "DELETE FROM films WHERE user_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, releaseDate = ?, duration = ?" +
                "WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery
                , film.getName()
                , film.getReleaseDate()
                , film.getDuration());

        return film;
    }

    @Override
    public Film getById(Long id) {
        String sqlQuery = "SELECT film_id, name, description, release_date, duration, mpa_rating FROM films WHERE film_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Film with ID {} not found", id);
            throw new RuntimeException("Фильм с ID " + id + " не найден.");
        } catch (DataAccessException e) {
            log.error("Database access error while retrieving film with ID {}", id, e);
            throw new RuntimeException("Ошибка доступа к базе данных.");
        }
    }

    @Override
    public Collection<Film> getAll() {
        String sqlQuery = "SELECT * FROM films";
        return jdbcTemplate.query(sqlQuery, filmRowMapper);
    }


}
