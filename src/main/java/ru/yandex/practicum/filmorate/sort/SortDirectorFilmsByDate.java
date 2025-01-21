package ru.yandex.practicum.filmorate.sort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SortDirectorFilmsByDate implements SortDirectorFilmsStrategy {

    @Override
    public String getSortSQL(int directorId) {
        String filmsSql = "SELECT " +
                "f.film_id AS film_id, " +
                "f.film_name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "r.mpa_id AS mpa_id, " +
                "r.mpa_name AS mpa_name " +
                "FROM Films AS f " +
                "LEFT JOIN Likes l ON f.film_id = l.film_id " +
                "JOIN MpaRatings r ON r.mpa_id = f.mpa_id " +
                "JOIN Films_directors fd on f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY f.release_date";
        return filmsSql;
    }
}
