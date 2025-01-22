package ru.yandex.practicum.filmorate.search;

public class SearchByTitle implements SearchStrategy {

    @Override
    public String doSearch(String query) {
        String title = "%" + query + "%";
        String sqlQuery = "SELECT " +
                "f.film_id AS film_id, " +
                "f.film_name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "m.mpa_id AS mpa_id, " +
                "m.mpa_name AS mpa_name, " +
                "g.genre_id AS genre_id, " +
                "g.genre_name AS genre_name " +
                "FROM Films AS f " +
                "JOIN MpaRatings m ON m.mpa_id = f.mpa_id " +
                "JOIN FilmGenres fg ON f.film_id = fg.film_id " +
                "JOIN Genres g ON fg.genre_id = g.genre_id " +
                "WHERE UPPER(f.film_name) LIKE UPPER('" + title + "') ";
        return sqlQuery;
    }
}
