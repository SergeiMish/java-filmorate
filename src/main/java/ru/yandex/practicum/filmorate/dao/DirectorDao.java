package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.DirectorStorage;
import ru.yandex.practicum.filmorate.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.exeption.ErrorMessages.DIRECTOR_NOT_FOUND;

@Repository
public class DirectorDao implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final DirectorRowMapper directorRowMapper;

    @Autowired
    public DirectorDao(final JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, final DirectorRowMapper directorRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.directorRowMapper = directorRowMapper;
    }

    @Override
    public List<Director> findAllDirectors() {
        return jdbcTemplate.query("SELECT * FROM directors", directorRowMapper);
    }

    @Override
    public Optional<Director> findDirectorById(int director_id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        try {
            Director director = jdbcTemplate.queryForObject(sql, directorRowMapper, director_id);
            return Optional.ofNullable(director);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean containsDirector(Integer directorId) {
        String sql = "SELECT COUNT(*) FROM directors WHERE director_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, directorId);
        return count != null && count > 0;
    }

    @Override
    public Director createDirector(Director director) {
        String sql = "SELECT director_id FROM directors WHERE director_id = ?;";
        SqlRowSet row = jdbcTemplate.queryForRowSet(sql, director.getId());
        if (row.next()) {
            throw new ValidationException("Режиссер  уже существует" + director.getId());
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");
        director.setId(simpleJdbcInsert.executeAndReturnKey(buildDirector(director)).intValue());
        return director;
    }

    public Map<String, Object> buildDirector(Director director) {
        Map<String, Object> values = new HashMap<>();
        values.put("director_name", director.getName());
        return values;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (rowsAffected == 0) {
            throw new NotFoundObjectException(DIRECTOR_NOT_FOUND + director.getId());
        }
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Film addDirectorOfFilm(Film film) {
        String sql = "INSERT INTO films_directors (film_id, director_id) VALUES (:film_id, :director_id)";
        SqlParameterSource[] batch = film.getDirector()
                .stream()
                .map(Director::getId)
                .map(directorId -> new MapSqlParameterSource()
                        .addValue("film_id", film.getId())
                        .addValue("director_id", directorId))
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, batch);
        return film;
    }

    @Override
    public List<Director> findDirectorForFilm(int filmId) {
        List<Integer> directorsId = new ArrayList<>();
        String sql = "SELECT director_id FROM films_directors WHERE film_id = ?";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, filmId);
        while (rows.next()) {
            directorsId.add(rows.getInt("director_id"));
        }
        SqlParameterSource parameters = new MapSqlParameterSource("ids", directorsId);
        return namedParameterJdbcTemplate.queryForStream(
                "SELECT director_id, director_name FROM directors WHERE director_id IN (:ids)",
                parameters,
                (rs, rowNum) -> new Director(rs.getInt("director_id"),
                        rs.getString("director_name"))).collect(Collectors.toList());
    }
}
