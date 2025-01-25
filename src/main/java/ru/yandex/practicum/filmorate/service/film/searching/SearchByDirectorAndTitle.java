package ru.yandex.practicum.filmorate.service.film.searching;

public class SearchByDirectorAndTitle implements SearchStrategy {

    @Override
    public String doSearch(String query) {
        String params = "%" + query + "%";
        String sqlQuery = "SELECT f.film_id AS film_id, f.film_name AS film_name, f.description AS description, " +
                "f.release_date AS release_date, f.duration AS duration, " +
                "r.mpa_id AS mpa_id, r.mpa_name AS mpa_name, " +
                "d.director_name AS director_name " +
                "FROM Films AS f " +
                "LEFT JOIN FilmDirectors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN Directors d ON fd.director_id = d.director_id " +
                "LEFT JOIN Likes fl ON f.film_id = fl.film_id " +
                "JOIN MpaRatings r ON r.mpa_id = f.mpa_id " +
                "WHERE (UPPER(d.director_name) LIKE UPPER('" + params + "') OR UPPER(f.film_name) " +
                "LIKE UPPER('" + params + "')) " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "r.mpa_id, r.mpa_name, d.director_name " +
                "ORDER BY COUNT(fl.film_id) DESC";
        return sqlQuery;
    }
}
