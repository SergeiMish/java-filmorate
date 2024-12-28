package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Repository
public class MpaRatingDao {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper mpaRatingRowMapper = new MpaRatingRowMapper();

    public MpaRatingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT name FROM MpaRatings";
        return jdbcTemplate.query(sql, mpaRatingRowMapper);
    }

    public MpaRating getMpaRatingById(Long id) {
        String sql = "SELECT name FROM MpaRatings WHERE mpa_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, mpaRatingRowMapper);
    }
}