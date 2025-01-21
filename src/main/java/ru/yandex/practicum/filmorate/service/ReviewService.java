package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.EventStorage;
import ru.yandex.practicum.filmorate.interfaces.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final EventStorage eventStorage;

    public Review addReview(Review review) {
        Review createdReview = reviewStorage.create(review);

        eventStorage.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("ADD")
                .entityId(createdReview.getReviewId())
                .build());

        return createdReview;
    }

    public Review updateReview(Long id, Review review) {
        Review updatedReview = reviewStorage.update(id, review);

        eventStorage.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("UPDATE")
                .entityId(id)
                .build());

        return updatedReview;
    }

    public void deleteReview(Long id) {
        Review review = reviewStorage.getById(id);
        reviewStorage.delete(id);

        eventStorage.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType("REVIEW")
                .operation("REMOVE")
                .entityId(id)
                .build());
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