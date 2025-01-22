package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.ReviewStorage;
import ru.yandex.practicum.filmorate.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ReviewDao implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    @Override
    public Review create(Review review) {
        validateUserAndFilmExistence(review.getUserId(), review.getFilmId());

        String sql = "INSERT INTO Reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.isPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM Reviews WHERE review_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Review update(Long id, Review review) {
        String sql = "UPDATE Reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.isPositive(), review.getUseful(), id);
        return review;
    }

    @Override
    public Review getById(Long id) {
        String sql = "SELECT * FROM Reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, reviewRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundObjectException("Review not found with id: " + id);
        }
    }

    @Override
    public List<Review> getReviews(Long filmId, int count) {
        String sql = filmId != null ?
                "SELECT * FROM Reviews WHERE film_id = ? LIMIT ?" :
                "SELECT * FROM Reviews LIMIT ?";
        return filmId != null ?
                jdbcTemplate.query(sql, reviewRowMapper, filmId, count) :
                jdbcTemplate.query(sql, reviewRowMapper, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        if (!reviewExists(reviewId)) {
            throw new NotFoundObjectException("Review with ID " + reviewId + " does not exist.");
        }

        String sql = "INSERT INTO ReviewLikes (review_id, user_id, is_like) VALUES (?, ?, true)";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUsefulCount(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        if (!reviewExists(reviewId)) {
            throw new NotFoundObjectException("Review with ID " + reviewId + " does not exist.");
        }

        String checkSql = "SELECT COUNT(*) FROM ReviewLikes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String updateSql = "UPDATE ReviewLikes SET is_like = false WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, reviewId, userId);
        } else {
            String insertSql = "INSERT INTO ReviewLikes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        }
        updateUsefulCount(reviewId);
    }

    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM ReviewLikes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUsefulCount(reviewId);
    }

    private void updateUsefulCount(Long reviewId) {
        String sql = "UPDATE Reviews SET useful = (SELECT SUM(CASE WHEN is_like THEN 1 ELSE -1 END) FROM ReviewLikes WHERE review_id = ?) WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM ReviewLikes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    public boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM Users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean filmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM Films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{filmId}, Integer.class);
        return count != null && count > 0;
    }
    private boolean reviewExists(Long reviewId) {
        String sql = "SELECT COUNT(*) FROM Reviews WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return count != null && count > 0;
    }

    private void validateUserAndFilmExistence(Long userId, Long filmId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        if (!userExists(userId)) {
            throw new NotFoundObjectException("User with ID " + userId + " does not exist.");
        }

        if (filmId == null) {
            throw new IllegalArgumentException("Film ID cannot be null.");
        }

        if (!filmExists(filmId)) {
            throw new NotFoundObjectException("Film with ID " + filmId + " does not exist.");
        }
    }
}