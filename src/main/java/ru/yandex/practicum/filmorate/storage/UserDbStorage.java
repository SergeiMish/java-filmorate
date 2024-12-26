package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User create(User user) {
        String sqlQuery = "insert into users(email, login, name, birthday) " +
                "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getBirthday().atStartOfDay()));
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public boolean delete(Long id) {
        String sqlQuery = "delete from users where user_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "update users set " +
                "email = ?, login = ?, name = ?, birthday = ?" +
                "where user_id = ?";
        jdbcTemplate.update(sqlQuery
                , user.getEmail()
                , user.getLogin()
                , user.getName()
                , Timestamp.valueOf(user.getBirthday().atStartOfDay()));
        return user;
    }

    @Override
    public User getById(Long id) {
        String sqlQuery = "select user_id, email, login, name, birthday from users where user_id = ?";

        return jdbcTemplate.queryForObject(sqlQuery, userRowMapper, id);
    }

    @Override
    public Collection<User> getAll() {
        String sqlQuery = "select user_id, email, login, name, birthday from users";
        return jdbcTemplate.query(sqlQuery, userRowMapper);
    }

    @Override
    public Set<User> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sqlQuery = String.format("SELECT user_id, email, login, name, birthday FROM users WHERE user_id IN (%s)", placeholders);

        List<User> users = jdbcTemplate.query(sqlQuery, userRowMapper, ids.toArray());

        return new HashSet<>(users);
    }
}
