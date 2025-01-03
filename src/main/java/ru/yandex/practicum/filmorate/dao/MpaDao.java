package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
public class MpaDao {
    private final JdbcTemplate jdbcTemplate;

    public MpaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAllMpaRatings() {
        String sql = "SELECT * FROM MpaRatings ORDER BY mpa_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getLong("mpa_id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });
    }

    public Mpa getMpaRatingById(Long id) {
        String sql = "SELECT * FROM MpaRatings WHERE mpa_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("mpa_id"));
                mpa.setName(rs.getString("name"));
                return mpa;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new MpaNotFoundException(id);
        }
    }
}