package ru.yandex.practicum.filmorate.service.sorting;

public class SortByReleaseDate implements SortStrategy {

    @Override
    public String getSortSQL(int directorId) {
        return "SELECT " +
                "f.film_id AS film_id, " +
                "f.film_name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "r.mpa_id AS mpa_id, " +
                "r.mpa_name AS mpa_name " +
                "FROM Films AS f " +
                "LEFT JOIN Likes fl ON f.film_id = fl.film_id " +
                "JOIN MpaRatings r ON r.mpa_id = f.mpa_id " +
                "LEFT JOIN FilmDirectors fd on f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY f.release_date";
    }
}