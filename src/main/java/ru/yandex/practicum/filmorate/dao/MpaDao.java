package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.MpaNotFoundException;
import ru.yandex.practicum.filmorate.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
public class MpaDao {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper mpaRatingRowMapper;

    @Autowired
    public MpaDao(JdbcTemplate jdbcTemplate, MpaRatingRowMapper mpaRatingRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRatingRowMapper = mpaRatingRowMapper;
    }

    public List<Mpa> getAllMpaRatings() {
        String sql = "SELECT * FROM MpaRatings ORDER BY mpa_id";
        return jdbcTemplate.query(sql, mpaRatingRowMapper);
    }

    public Mpa getMpaRatingById(Long id) {
        String sql = "SELECT * FROM MpaRatings WHERE mpa_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, mpaRatingRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new MpaNotFoundException(id);
        }
    }
}