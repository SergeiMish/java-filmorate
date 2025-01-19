package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.mapper.ReviewDtoMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@RequestBody @Valid ReviewDto reviewDto) {
        Review review = ReviewDtoMapper.toModel(reviewDto);
        Review createdReview = reviewService.addReview(review);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(createdReview));
    }

    @PutMapping
    public ResponseEntity<ReviewDto> updateReview(@RequestBody @Valid ReviewDto reviewDto) {
        if (reviewDto.getReviewId() == null) {
            throw new IllegalArgumentException("Review ID cannot be null.");
        }
        Review review = ReviewDtoMapper.toModel(reviewDto);
        Review updatedReview = reviewService.updateReview(reviewDto.getReviewId(), review);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(updatedReview));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ReviewDtoMapper.toDto(review));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getReviews(@RequestParam(required = false) Long filmId,
                                                      @RequestParam(defaultValue = "10") int count) {
        List<Review> reviews = reviewService.getReviews(filmId, count);
        List<ReviewDto> reviewDtos = reviews.stream()
                .map(ReviewDtoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviewDtos);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> likeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.likeReview(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> dislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.dislikeReview(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.removeDislike(id, userId);
        return ResponseEntity.ok().build();
    }
}