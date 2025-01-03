package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


@Repository
@RequiredArgsConstructor
public class UserDao implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getBirthday().atStartOfDay()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return user;
    }

    @Override
    public boolean delete(Long id) {
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    public User update(User user) {
        String sqlQuery = "UPDATE users SET " +
                "email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Timestamp.valueOf(user.getBirthday().atStartOfDay()),
                user.getId()
        );

        if (rowsAffected == 0) {
            throw new NotFoundObjectException("Не найден пользователь с ID: " + user.getId());
        }

        return user;
    }

    @Override
    public User getById(Long id) {
        String sqlQuery = "SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundObjectException("User not found with id: " + id);
        }
    }

    @Override
    public Collection<User> getAll() {
        String sqlQuery = "SELECT user_id, email, login, name, birthday FROM users ORDER BY user_id ASC";
        return jdbcTemplate.query(sqlQuery, userRowMapper);
    }

    public void addFriend(Long user1Id, Long user2Id) {
        String sqlQueryAddFriend = "INSERT INTO friendships(user1_id, user2_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQueryAddFriend, user1Id, user2Id);
    }

    public void removeFriend(Long user1Id, Long user2Id) {
        String sqlQuery = "DELETE FROM friendships WHERE user1_id = ? AND user2_id = ?";
        jdbcTemplate.update(sqlQuery, user1Id, user2Id);
    }

    public List<Long> getFriendIds(Long userId) {
        String sqlQueryUser2 = "SELECT user2_id FROM friendships WHERE user1_id = ?";
        return jdbcTemplate.queryForList(sqlQueryUser2, Long.class, userId);
    }

    public boolean isFriendshipExists(Long user1Id, Long user2Id) {
        String sqlQuery = "SELECT COUNT(*) FROM friendships WHERE user1_id = ? AND user2_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, user1Id, user2Id);
        return count != null && count > 0;
    }
}
