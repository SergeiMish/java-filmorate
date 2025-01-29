package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public interface RatingStorage {

    List<Mpa> findAllMpaRatings();

    boolean containsRating(Integer ratingId);

    Optional<Mpa> findMpaRatingById(int id);

}
