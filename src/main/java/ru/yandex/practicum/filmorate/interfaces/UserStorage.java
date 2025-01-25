package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getAll();

    User create(User user);

    void delete(Integer id);

    User update(User newUser);

    boolean contains(Integer id);

    Optional<User> getById(int id);

    List<User> getFriendsByUserId(int userId);

    List<User> getCommonFriends(int userId, int friendId);

    void addFriendship(Integer userId, Integer friendId);

    void deleteFriendship(Integer userId, Integer friendId);

    void deleteAll();
}
