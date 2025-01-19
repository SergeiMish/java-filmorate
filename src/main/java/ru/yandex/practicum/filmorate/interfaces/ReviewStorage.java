package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review create(Review review);

    boolean delete(Long id);

    Review update(Long id, Review review);

    Review getById(Long id);

    List<Review> getReviews(Long filmId, int count);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);
}