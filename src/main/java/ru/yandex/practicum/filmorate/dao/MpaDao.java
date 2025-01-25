package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.RatingStorage;
import ru.yandex.practicum.filmorate.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDao implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;

    private final MpaRatingRowMapper ratingRowMapper;

    @Override
    public List<Mpa> findAllMpaRatings() {
        String sql = "SELECT * FROM MpaRatings r " +
                    "ORDER BY r.mpa_id;";
        return jdbcTemplate.query(sql, ratingRowMapper);
    }

    @Override
    public boolean containsRating(Integer ratingId) {
        String sql = "SELECT COUNT(*) FROM MpaRatings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ratingId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Mpa> findMpaRatingById(int id) {
        String sqlQuery = "SELECT * FROM MpaRatings WHERE mpa_id = ?";
        try {
            Mpa rating = jdbcTemplate.queryForObject(sqlQuery, ratingRowMapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}