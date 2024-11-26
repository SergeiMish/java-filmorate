package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final InMemoryUserStorage inMemoryUserStorage;

    public UserService(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }


    public User addFriend(Long id, Long friendId) {
        User user = inMemoryUserStorage.getUserById(id);
        User friend = inMemoryUserStorage.getUserById(friendId);
        if (user == null) {
            throw new NotFoundObjectException("ID " + id + " не найден");
        }
        if (friend == null) {
            throw new NotFoundObjectException("ID " + friendId + " не найден");
        }

        if (!user.getFriends().contains(friendId)) {
            user.getFriends().add(friendId);
            friend.getFriends().add(id);
            inMemoryUserStorage.updateUser(user);
            inMemoryUserStorage.updateUser(friend);
        }

        return user;
    }

    public User removeFromFriends(Long id, Long friendId) {
        User user = inMemoryUserStorage.getUserById(id);
        User friend = inMemoryUserStorage.getUserById(friendId);

        if (user == null) {
            throw new NotFoundObjectException("Пользователь с ID " + id + " не найден");
        }
        if (friend == null) {
            throw new NotFoundObjectException("Пользователь с ID " + friendId + " не найден");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        inMemoryUserStorage.updateUser(user);
        inMemoryUserStorage.updateUser(friend);

        return user;
    }

    public Set<User> listFriends(Long id) {
        User user = inMemoryUserStorage.getUserById(id);
        if (user != null) {
            return user.getFriends().stream()
                    .map(inMemoryUserStorage::getUserById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        throw new NotFoundObjectException("Объект не найден");
    }

    public Set<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = inMemoryUserStorage.getUserById(userId);
        User otherUser = inMemoryUserStorage.getUserById(otherUserId);

        if (user == null || otherUser == null) {
            throw new NotFoundObjectException("Объект не найден");
        }

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(inMemoryUserStorage::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
