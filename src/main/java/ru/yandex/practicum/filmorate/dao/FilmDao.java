package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;


@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmDao implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film create(Film film) {

        validateMpaExists(film.getMpa().getId());

        String sqlQuery = "INSERT INTO Films (name, description, release_date, duration, mpa_rating) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.debug("Executing SQL: {}", sqlQuery);
        log.debug("With parameters: name={}, description={}, releaseDate={}, duration={}, mpaRating={}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa());

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<>());
        }

        log.info("Film created with ID: {}", film.getId());

        return film;
    }

    @Override
    public boolean delete(Long id) {
        String sqlQuery = "DELETE FROM Films WHERE film_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE Films SET " +
                "name = ?, description = ?, release_date = ?, duration = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        return film;
    }

    @Override
    public Film getById(Long id) {
        String sqlQuery = "SELECT film_id, name, description, release_date, duration, mpa_id FROM Films WHERE film_id = ?";
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
        String sqlQuery = "SELECT * FROM Films";
        return jdbcTemplate.query(sqlQuery, filmRowMapper);
    }

    public void validateMpaExists(Long mpaId) {
        log.info("Проверка существования mpa_id = {} в таблице MpaRatings", mpaId);
        final String sqlQueryMpa = "SELECT COUNT(*) FROM MpaRatings WHERE mpa_id = ?";

        Integer count = jdbcTemplate.queryForObject(sqlQueryMpa, Integer.class, mpaId);

        if (count == null || count == 0) {
            throw new ValidationException("MPA id не существует");
        }
    }
}
