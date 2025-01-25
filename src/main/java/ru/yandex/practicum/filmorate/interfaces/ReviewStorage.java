package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review newReview);

    void delete(int id);

    void removeAll();

    List<Review> getReviews();

    Optional<Review> findById(int id);

    List<Review> findByFilmId(int id, int size);

    void addRating(int reviewId, int userId, boolean isLike);

    void removeRating(int id, int userId, boolean isLike);

}
