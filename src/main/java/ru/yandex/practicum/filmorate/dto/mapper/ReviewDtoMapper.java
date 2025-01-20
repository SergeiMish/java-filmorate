package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public class ReviewDtoMapper {

    public static ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .isPositive(review.isPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

    public static Review toModel(ReviewDto reviewDto) {
        if (reviewDto == null) {
            return null;
        }
        return Review.builder()
                .reviewId(reviewDto.getReviewId() != null ? reviewDto.getReviewId() : 0L)
                .content(reviewDto.getContent())
                .isPositive(reviewDto.getIsPositive())
                .userId(reviewDto.getUserId())
                .filmId(reviewDto.getFilmId())
                .useful(reviewDto.getUseful())
                .build();
    }
}