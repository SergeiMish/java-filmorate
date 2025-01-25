package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.searching.SearchStrategy;
import ru.yandex.practicum.filmorate.service.film.searching.SearchingFilms;
import ru.yandex.practicum.filmorate.service.sorting.SortDirectorFilms;
import ru.yandex.practicum.filmorate.service.sorting.SortDirectorFilmsStrategy;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class FilmDao implements FilmStorage {
    private final SearchingFilms searchingFilms;
    private final SortDirectorFilms sortDirectorFilms;
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Film> findAllFilms() {
        return jdbcTemplate.query("SELECT " +
                        "f.film_id film_id, " +
                        "f.film_name film_name, " +
                        "f.description description, " +
                        "f.release_date release_date, " +
                        "f.duration duration, " +
                        "r.mpa_id mpa_id, " +
                        "r.mpa_name mpa_name " +
                        "FROM Films f " +
                        "INNER JOIN MpaRatings r ON f.mpa_id = r.mpa_id; ",
                filmRowMapper);
    }

    @Override
    public Film add(Film film) {
        String addFilmsql = "INSERT INTO Films (film_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(addFilmsql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setLong(4, film.getDuration());
                ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : 5, Types.INTEGER);

                return ps;
            }, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(e.getMessage());
        }

        int generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(generatedId);

        return film;
    }

    @Override
    public void removeFilm(Integer filmId) {
        String deleteFilmSql = "DELETE FROM Films WHERE film_id = ?";
        jdbcTemplate.update(deleteFilmSql, filmId);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE Films SET " +
                "film_name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsAffected == 0) {
            throw new NotFoundObjectException(FILM_NOT_FOUND + film.getId());
        }
        return film;
    }

    @Override
    public boolean containsFilm(Integer filmId) {
        String sql = "SELECT COUNT(*) FROM Films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sqlQuery = "SELECT " +
                "f.film_id as film_id, " +
                "f.film_name as film_name, " +
                "f.description as description, " +
                "f.release_date as release_date, " +
                "f.duration as duration, " +
                "r.mpa_id as mpa_id, " +
                "r.mpa_name as mpa_name " +
                "FROM Films f " +
                "INNER JOIN MpaRatings r ON f.mpa_id = r.mpa_id " +
                "WHERE f.film_id = ?; ";
        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO Likes (film_id, liked_user_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM Likes WHERE film_id = ? AND liked_user_id = ?";

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int size) {
        String filmsSql = "SELECT " +
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
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY COUNT(fl.film_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(filmsSql, filmRowMapper, size);
    }

    @Override
    public List<Film> getFilmsByDirectorSorted(int directorId, SortDirectorFilmsStrategy sortDirectorFilmsStrategy) {
        sortDirectorFilms.setSearchStrategy(sortDirectorFilmsStrategy);
        return jdbcTemplate.query(sortDirectorFilms.searchFilms(directorId), filmRowMapper, directorId);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String filmsSql = "SELECT " +
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
                "WHERE f.film_id IN (SELECT fl2.film_id " +
                "FROM Likes fl " +
                "JOIN Likes fl2 ON fl.film_id = fl2.film_id " +
                "WHERE fl.liked_user_id = ? " +
                "AND fl2.liked_user_id = ?) " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY COUNT(fl.LIKED_USER_ID) DESC ";
        return jdbcTemplate.query(filmsSql, filmRowMapper, userId, friendId);
    }

    @Override
    public void removeAll() {
        String removeFilmsSql = "DELETE FROM Films";
        jdbcTemplate.update(removeFilmsSql);
    }

    @Override
    public List<Film> searchFilmsBy(String query, SearchStrategy searchStrategy) {
        searchingFilms.setSearchStrategy(searchStrategy);
        return jdbcTemplate.query(searchingFilms.searchFilms(query), filmRowMapper);
    }

    @Override
    public List<Film> getPopularFilmsSortedByGenreAndYear(Integer count, Integer genreId, Integer year) {
        String sqlQuery = "SELECT " +
                "f.film_id AS film_id, " +
                "f.film_name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "r.mpa_id AS mpa_id, " +
                "r.mpa_name AS mpa_name " +
                "FROM Films AS f " +
                "LEFT JOIN FilmGenres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN Likes fl ON f.film_id = fl.film_id " +
                "JOIN MpaRatings r ON r.mpa_id = f.mpa_id " +
                "WHERE (fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ?) " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY COUNT(f.film_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, filmRowMapper, genreId, year, count);
    }

    @Override
    public List<Film> getPopularFilmsSortedByYear(Integer count, Integer year) {
        String sqlQuery = "SELECT " +
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
                "WHERE EXTRACT(YEAR FROM f.release_date) = ? " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY COUNT(f.film_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, filmRowMapper, year, count);
    }

    @Override
    public List<Film> getPopularFilmsSortedByGenre(Integer count, Integer genreId) {
        String sqlQuery = "SELECT " +
                "f.film_id AS film_id, " +
                "f.film_name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "r.mpa_id AS mpa_id, " +
                "r.mpa_name AS mpa_name " +
                "FROM Films AS f " +
                "LEFT JOIN FilmGenres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN Likes fl ON f.film_id = fl.film_id " +
                "JOIN MpaRatings r ON r.mpa_id = f.mpa_id " +
                "WHERE fg.genre_id = ? " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, r.mpa_id, r.mpa_name " +
                "ORDER BY COUNT(f.film_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, filmRowMapper, genreId, count);
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        String sqlQuery = "SELECT liked_user_id FROM Likes WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getInt("liked_user_id"), filmId);
    }

    @Override
    public boolean checkLikesUserByFilmId(Integer filmId, Integer userId) {
        String sqlQuery = "SELECT COUNT(*) FROM Likes WHERE film_id = ? AND liked_user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public List<Film> getFilmRecommendationsForUser(int userId) {
        String sql = "SELECT DISTINCT f.film_id AS film_id, " +
                "                f.film_name AS film_name, " +
                "                f.description AS description, " +
                "                f.release_date AS release_date, " +
                "                f.duration AS duration, " +
                "                r.mpa_id AS mpa_id, " +
                "                r.mpa_name AS mpa_name " +
                "FROM Likes fl2 " +
                "JOIN ( " +
                "    SELECT fl1.liked_user_id AS common_user_id, " +
                "           COUNT(*) AS common_likes_cnt " +
                "    FROM Likes fl1 " +
                "    JOIN ( " +
                "        SELECT DISTINCT film_id " +
                "        FROM Likes " +
                "        WHERE liked_user_id = ? " +
                "    ) AS ul ON fl1.film_id = ul.film_id " +
                "    WHERE fl1.liked_user_id != ? " +
                "    GROUP BY fl1.liked_user_id " +
                "    HAVING COUNT(*) = ( " +
                "        SELECT MAX(common_likes_cnt) " +
                "        FROM ( " +
                "            SELECT COUNT(*) AS common_likes_cnt " +
                "            FROM Likes fl1 " +
                "            JOIN ( " +
                "                SELECT DISTINCT film_id " +
                "                FROM Likes " +
                "                WHERE liked_user_id = ? " +
                "            ) AS ul ON fl1.film_id = ul.film_id " +
                "            WHERE fl1.liked_user_id != ? " +
                "            GROUP BY fl1.liked_user_id " +
                "        ) AS common_likes " +
                "    ) " +
                ") AS tcu ON fl2.liked_user_id = tcu.common_user_id " +
                "JOIN Films f ON fl2.film_id = f.film_id " +
                "JOIN MpaRatings r ON f.mpa_id = r.mpa_id " +
                "WHERE fl2.film_id NOT IN ( " +
                "    SELECT film_id " +
                "    FROM Likes " +
                "    WHERE liked_user_id = ? " +
                ")";

        return jdbcTemplate.query(sql, filmRowMapper, userId, userId, userId, userId, userId);
    }
}
