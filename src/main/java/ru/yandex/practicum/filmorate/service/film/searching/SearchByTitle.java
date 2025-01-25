package ru.yandex.practicum.filmorate.service.film.searching;

public class SearchByTitle implements SearchStrategy {

    @Override
    public String doSearch(String query) {
        String title = "%" + query + "%";
        String sqlQuery = "SELECT f.film_id AS film_id, f.film_name AS film_name, f.description AS description, " +
                "f.release_date AS release_date, f.duration AS duration, " +
                "r.mpa_id AS mpa_id, r.mpa_name AS mpa_name, " +
                "g.genre_name AS genre_name, g.genre_id AS genre_id " +
                "FROM Films AS f " +
                "LEFT JOIN FilmGenres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN Genres g ON g.genre_id = fg.genre_id " +
                "LEFT JOIN Likes fl ON f.film_id = fl.film_id " +
                "JOIN MpaRatings r ON r.mpa_id = f.mpa_id " +
                "LEFT JOIN FilmDirectors fd ON f.film_id = fd.film_id " +
                "WHERE UPPER(f.film_name) LIKE UPPER('" + title + "') " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                "r.mpa_id, r.mpa_name, g.genre_name " +
                "ORDER BY COUNT(fl.film_id) DESC;";
        return sqlQuery;
    }
}
