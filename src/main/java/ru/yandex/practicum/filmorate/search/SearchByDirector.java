package ru.yandex.practicum.filmorate.search;

public class SearchByDirector implements SearchStrategy {

    @Override
    public String doSearch(String query) {
        String directorName = "%" + query + "%";
        String sqlQuery = "SELECT f.film_id AS film_id, f.film_name AS film_name, f.description AS description, " +
                "f.release_date AS release_date, f.duration AS duration, " +
                "m.mpa_id AS mpa_id, m.mpa_name AS mpa_name, " +
                "d.director_name AS director_name, " +
                "g.genre_id AS genre_id, " +
                "g.genre_name AS genre_name " +
                "FROM Films AS f " +
                "LEFT JOIN Films_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN Directors d ON fd.director_id = d.director_id " +
                "JOIN MpaRatings m ON m.mpa_id = f.mpa_id " +
                "JOIN FilmGenres fg ON f.film_id = fg.film_id " +
                "JOIN Genres g ON fg.genre_id = g.genre_id " +
                "WHERE UPPER(d.director_name) LIKE UPPER('" + directorName + "') ";
        return sqlQuery;
    }
}
