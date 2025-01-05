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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmDao implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film create(Film film) {
        validateMpaExists(film.getMpa().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenresExist(film.getGenres());
        }

        String sqlQuery = "INSERT INTO Films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
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
        String sqlQuery = "DELETE FROM Films WHERE film_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundObjectException("Фильм с ID " + film.getId() + " не найден");
        }

        Long mpaId = Optional.ofNullable(film.getMpa())
                .map(Mpa::getId)
                .orElse(null);

        String sqlQuery = "UPDATE Films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId());

        return film;
    }

    @Override
    public Film getById(Long id) {
        String sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                "g.genre_id, g.name AS genre_name " +
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
                                .name(rs.getString("name"))
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
        String sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM Films f " +
                "JOIN MpaRatings m ON f.mpa_id = m.mpa_id " +
                "ORDER BY f.film_id";
        return jdbcTemplate.query(sqlQuery, filmRowMapper);
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
}