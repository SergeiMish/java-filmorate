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
        User user = getUserOrThrow(id);
        User friend = getUserOrThrow(friendId);

        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }

        if (user.addFriend(friendId)) {
            friend.addFriend(id);
            userStorage.update(friend);
            userStorage.update(user);
        }

        return user;
    }

    public User removeFriend(Long id, Long friendId) {
        User user = getUserOrThrow(id);
        User friend = getUserOrThrow(friendId);

        if (user.removeFriend(friendId)) {
            friend.removeFriend(id);
            userStorage.update(user);
            userStorage.update(friend);
        }

        return user;
    }

    public User getUserOrThrow(Long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundObjectException("ID " + id + " не найден");
        }
        return user;
    }

    public Set<User> listFriends(Long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundObjectException("Объект не найден");
        }

        return user.getFriends().stream()
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.getById(userId);
        User otherUser = userStorage.getById(otherUserId);

        if (user == null || otherUser == null) {
            throw new NotFoundObjectException("Объект не найден");
        }

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        Set<Long> commonFriendIds = userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toSet());

        return userStorage.findByIds(commonFriendIds);
    }
}
