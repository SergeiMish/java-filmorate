package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.DatabaseAccessException;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDao implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film create(Film film) {
        validateMpaExists(film.getMpa().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenresExist(film.getGenres());
        }
        if (film.getGenres() != null) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            film.setGenres(new ArrayList<>(uniqueGenres));
        }

        String sqlQuery = "INSERT INTO Films (film_name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.debug("Executing SQL: {}", sqlQuery);
        log.debug("With parameters: name={}, description={}, releaseDate={}, duration={}, mpaId={}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        String insertGenresSql = "INSERT INTO FilmGenres (film_id, genre_id) VALUES (?, ?)";
        Set<Long> uniqueGenreIds = new HashSet<>();
        List<Object[]> batchArgs = new ArrayList<>();

        for (Genre genre : film.getGenres()) {
            if (uniqueGenreIds.add(genre.getId())) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
        }

        jdbcTemplate.batchUpdate(insertGenresSql, batchArgs);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<>());
        }

        log.info("Film created with ID: {}", film.getId());

        return film;
    }

    @Override
    public boolean delete(Long id) {
        String deleteLikesSql = "DELETE FROM Likes WHERE film_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);

        String deleteGenresSql = "DELETE FROM FilmGenres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, id);

        String deleteFilmSql = "DELETE FROM Films WHERE film_id = ?";
        return jdbcTemplate.update(deleteFilmSql, id) > 0;
    }

    @Override
    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundObjectException("Фильм с ID " + film.getId() + " не найден");
        }

        Long mpaId = Optional.ofNullable(film.getMpa())
                .map(Mpa::getId)
                .orElse(null);

        String sqlQuery = "UPDATE Films SET film_name = ?, " +
                "description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId());

        String deleteGenresSql = "DELETE FROM FilmGenres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());

        film.setGenres(removeDuplicateGenres(film.getGenres()));

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = new HashSet<>();
            List<Object[]> batchArgs = new ArrayList<>();

            for (Genre genre : film.getGenres()) {
                if (uniqueGenreIds.add(genre.getId())) {
                    batchArgs.add(new Object[]{film.getId(), genre.getId()});
                }
            }

            String insertGenresSql = "INSERT INTO FilmGenres (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(insertGenresSql, batchArgs);
        }

        return film;
    }

    @Override
    public List<Film> getFilmsByUserId(Long userId) {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, f.mpa_id, m.mpa_name " +
                "FROM Films f " +
                "JOIN Likes l ON f.film_id = l.film_id " +
                "JOIN MpaRatings m ON f.mpa_id = m.mpa_id " +
                "WHERE l.user_id = ?";

        List<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper, userId);

        Map<Long, List<Genre>> genresByFilmId = loadGenresForFilms();
        for (Film film : films) {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new ArrayList<>());
            film.setGenres(genres);
        }
        return films;
    }

    @Override
    public Film getById(Long id) {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, " +
                "f.release_date, f.duration, f.mpa_id, m.mpa_name, " +
                "g.genre_id, g.genre_name " +
                "FROM Films f " +
                "JOIN MpaRatings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN FilmGenres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN Genres g ON fg.genre_id = g.genre_id " +
                "WHERE f.film_id = ? " +
                "ORDER BY g.genre_id";

        try {
            return jdbcTemplate.query(sqlQuery, rs -> {
                Film film = null;
                Set<Genre> genreSet = new TreeSet<>(Comparator.comparingLong(Genre::getId));
                while (rs.next()) {
                    if (film == null) {
                        Mpa mpa = Mpa.builder()
                                .id(rs.getLong("mpa_id"))
                                .name(rs.getString("mpa_name"))
                                .build();

                        film = Film.builder()
                                .id(rs.getLong("film_id"))
                                .name(rs.getString("film_name"))
                                .description(rs.getString("description"))
                                .releaseDate(rs.getDate("release_date").toLocalDate())
                                .duration(rs.getInt("duration"))
                                .mpa(mpa)
                                .likes(new HashSet<>())
                                .genres(new ArrayList<>())
                                .build();
                    }
                    Long genreId = rs.getLong("genre_id");
                    if (genreId != null && genreId > 0) {
                        Genre genre = Genre.builder()
                                .id(genreId)
                                .name(rs.getString("genre_name"))
                                .build();
                        genreSet.add(genre);
                    }
                }
                if (film != null) {
                    film.setGenres(new ArrayList<>(genreSet));
                }
                return film;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Film with ID {} not found", id);
            throw new NotFoundObjectException("Фильм с ID " + id + " не найден.");
        } catch (DataAccessException e) {
            log.error("Database access error while retrieving film with ID {}", id, e);
            throw new DatabaseAccessException("Ошибка доступа к базе данных.", e);
        }
    }

    @Override
    public Collection<Film> getAll() {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.mpa_name " +
                "FROM Films f " +
                "JOIN MpaRatings m ON f.mpa_id = m.mpa_id " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper);

        Map<Long, List<Genre>> genresByFilmId = loadGenresForFilms();
        for (Film film : films) {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new ArrayList<>());
            film.setGenres(genres);
        }
        return films;
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sqlQuery = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.mpa_name " +
                "FROM Films f " +
                "JOIN FilmsDirectors fd ON f.film_id = fd.film_id " +
                "JOIN MpaRatings m ON f.mpa_id = m.mpa_id " +
                "WHERE fd.id = ? ";

        if ("year".equals(sortBy)) {
            sqlQuery += "ORDER BY f.release_date";
        } else if ("likes".equals(sortBy)) {
            sqlQuery += "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count FROM FilmLikes GROUP BY film_id) fl " +
                    "ON f.film_id = fl.film_id " +
                    "ORDER BY fl.like_count DESC";
        } else {
            throw new ValidationException("Некорректное значение сортировки: " + sortBy);
        }

        return jdbcTemplate.query(sqlQuery, filmRowMapper, directorId);
    }

    private Map<Long, List<Director>> loadDirectorsForFilms() {
        String sqlQuery = "SELECT fd.film_id, d.id, d.name " +
                "FROM FilmsDirectors fd " +
                "JOIN Directors d ON fd.id = d.id";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQuery);

        return rows.stream().collect(Collectors.groupingBy(
                row -> (Long) row.get("film_id"),
                Collectors.mapping(row -> Director.builder()
                        .id((Long) row.get("id"))
                        .name((String) row.get("name"))
                        .build(), Collectors.toList())
        ));
    }

    @Override
    public List<Film> getFilmsByDirectorAndOrByTitle(String query, String by) {
        String sqlQuery = "SELECT f.id, f.name, f.description, f.releaseDate, f.duration, f.mpa_id FROM films f";

        List<String> whereQuery = new ArrayList<>();
        if (by.contains("director")) {
            sqlQuery += " LEFT JOIN FilmsDirectors f_d " +
                    " ON f_d.film_id = f.id " +
                    " LEFT JOIN directors d " +
                    " ON f_d.id = d.id ";
            whereQuery.add(" d.name ilike '%" + query + "%' ");
        }

        if (by.contains("title")) {
            whereQuery.add(" f.name ilike '%" + query + "%' ");
        }

        if (whereQuery.isEmpty()) {
            throw new NotFoundObjectException("Неизвестное значение переменной by = " + by);
        }

        sqlQuery += " LEFT JOIN ( " +
                "SELECT film_id, COUNT(user_id) AS likes FROM film_like " +
                "GROUP BY film_id ) l " +
                "ON l.film_id = f.id " +
                "WHERE" + String.join(" or ", whereQuery) +
                "ORDER BY l.likes DESC ";

        return jdbcTemplate.query(sqlQuery, filmRowMapper::mapRow);
    }

    private Map<Long, List<Genre>> loadGenresForFilms() {
        String sqlQuery = "SELECT fg.film_id, g.genre_id, g.genre_name " +
                "FROM FilmGenres fg " +
                "JOIN Genres g ON fg.genre_id = g.genre_id";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlQuery);

        return rows.stream().collect(Collectors.groupingBy(
                row -> (Long) row.get("film_id"),
                Collectors.mapping(row -> Genre.builder()
                        .id((Long) row.get("genre_id"))
                        .name((String) row.get("genre_name"))
                        .build(), Collectors.toList())
        ));
    }

    private void saveFilmDirectors(Film film) {
        String deleteDirectorsSql = "DELETE FROM FilmsDirectors WHERE film_id = ?";
        jdbcTemplate.update(deleteDirectorsSql, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String insertDirectorsSql = "INSERT INTO FilmsDirectors (film_id, id) VALUES (?, ?)";
            List<Object[]> batchArgs = film.getDirectors().stream()
                    .map(director -> new Object[]{film.getId(), director.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(insertDirectorsSql, batchArgs);
        }
    }

    private void saveFilmGenres(Long filmId, List<Genre> genres) {
        String insertGenresSql = "INSERT INTO FilmGenres (film_id, genre_id) VALUES (?, ?)";
        Set<Long> uniqueGenreIds = new HashSet<>();
        List<Object[]> batchArgs = new ArrayList<>();

        for (Genre genre : genres) {
            if (uniqueGenreIds.add(genre.getId())) {
                batchArgs.add(new Object[]{filmId, genre.getId()});
            }
        }

        jdbcTemplate.batchUpdate(insertGenresSql, batchArgs);
    }

    private void validateMpaExists(Long mpaId) {
        log.info("Проверка существования mpa_id = {} в таблице MpaRatings", mpaId);
        final String sqlQueryMpa = "SELECT COUNT(*) FROM MpaRatings WHERE mpa_id = ?";

        Integer count = jdbcTemplate.queryForObject(sqlQueryMpa, Integer.class, mpaId);

        Optional.ofNullable(count)
                .filter(c -> c > 0)
                .orElseThrow(() -> new ValidationException("MPA id не существует"));
    }

    private void validateGenresExist(Collection<Genre> genres) {
        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        String sqlQuery = String.format("SELECT genre_id FROM Genres WHERE genre_id IN (%s)",
                genreIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));

        List<Long> existingIds = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getLong("genre_id"));

        if (existingIds.size() != genreIds.size()) {
            genreIds.removeAll(existingIds);
            throw new ValidationException("Некоторые жанры не существуют: " + genreIds);
        }
    }

    private boolean filmExists(Long filmId) {
        String sqlQuery = "SELECT COUNT(*) FROM Films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, filmId);
        return count != null && count > 0;
    }

    private List<Genre> removeDuplicateGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return genres;
        }

        Map<Long, Genre> uniqueGenres = new HashMap<>();
        for (Genre genre : genres) {
            uniqueGenres.put(genre.getId(), genre);
        }

        return new ArrayList<>(uniqueGenres.values());
    }
}
