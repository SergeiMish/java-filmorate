package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundObjectException("ID " + id + " не найден");
        }

        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundObjectException("ID " + friendId + " не найден");
        }

        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }

        if (user.addFriend(friendId)) {
            friend.addFriend(id);

            userStorage.updateUser(friend);
            userStorage.updateUser(user);
        }

        return user;
    }

    public User removeFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);

        if (user == null) {
            throw new NotFoundObjectException("ID " + id + " не найден");
        }
        if (friend == null) {
            throw new NotFoundObjectException("ID " + friendId + " не найден");
        }

        if (user.removeFriend(friendId)) {
            friend.removeFriend(id);
            userStorage.updateUser(user);
            userStorage.updateUser(friend);
        }

        return user;
    }

    public Set<User> listFriends(Long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundObjectException("Объект не найден");
        }

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherUserId);

        if (user == null || otherUser == null) {
            throw new NotFoundObjectException("Объект не найден");
        }

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
