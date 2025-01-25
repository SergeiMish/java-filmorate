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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class UserDao implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("SELECT * FROM Users", userRowMapper);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO Users (user_name, login, email, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(generatedId);

        return user;
    }

    @Override
    public void delete(Integer id) {
        String deleteUserSql = "DELETE FROM Users WHERE user_id = ?";
        jdbcTemplate.update(deleteUserSql, id);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE Users SET " +
                "user_name = ?, login = ?, email = ?, birthday = ? " +
                "WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        if (rowsAffected == 0) {
            throw new NotFoundObjectException(USER_NOT_FOUND + user.getId());
        }
        return user;
    }

    @Override
    public boolean contains(Integer id) {
        String sql = "SELECT COUNT(*) FROM Users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<User> getFriendsByUserId(int userId) {
        if (!this.contains(userId)) {
            throw new NotFoundObjectException("Can't find friends of non-existing user");
        }

        String friends = "SELECT * FROM Users " +
                "WHERE user_id IN (SELECT friend_id FROM Friendships WHERE user_id = ?);";

        return jdbcTemplate.query(friends, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        String sql = "SELECT u.* " +
                "FROM Users AS u " +
                "JOIN Friendships AS fs1 ON u.user_id = fs1.friend_id " +
                "JOIN Friendships AS fs2 ON u.user_id = fs2.friend_id " +
                "WHERE fs1.user_id = ? AND fs2.user_id = ?;";

        return jdbcTemplate.query(sql, userRowMapper, userId, friendId);
    }

    @Override
    public void addFriendship(Integer userId, Integer friendId) {
        String sql = "INSERT INTO Friendships (user_id, friend_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriendship(Integer userId, Integer friendId) {
        String sql = "DELETE FROM Friendships WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM Users";
        jdbcTemplate.update(sql);
    }
}
