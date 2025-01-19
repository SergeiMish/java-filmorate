package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public Review addReview(Review review) {
        return reviewStorage.create(review);
    }

    public Review updateReview(Long id, Review review) {
        return reviewStorage.update(id, review);
    }

    public void deleteReview(Long id) {
        reviewStorage.delete(id);
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getReviews(Long filmId, int count) {
        return reviewStorage.getReviews(filmId, count);
    }

    public void likeReview(Long reviewId, Long userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    public void dislikeReview(Long reviewId, Long userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewStorage.removeDislike(reviewId, userId);
    }
}