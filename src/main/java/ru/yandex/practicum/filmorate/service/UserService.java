package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserDao userDao;

    public User addFriend(Long id, Long friendId) {
        getUserOrThrow(id);
        getUserOrThrow(friendId);

        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }

        userDao.addFriendship(id, friendId, FriendshipStatus.UNCONFIRMED);

        if (userDao.isFriendshipExists(friendId, id, FriendshipStatus.UNCONFIRMED)) {
            confirmFriendship(id, friendId);
        }

        log.debug("Friendship added between user {} and user {}", id, friendId);

        return getUserOrThrow(id);
    }


    public User removeFriend(Long id, Long friendId) {
        getUserOrThrow(id);
        getUserOrThrow(friendId);

        userStorage.removeFriendship(id, friendId);
        userStorage.removeFriendship(friendId, id);

        return getUserOrThrow(id);
    }

    public void confirmFriendship(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userDao.updateFriendshipStatus(userId, friendId, FriendshipStatus.CONFIRMED);
        userDao.updateFriendshipStatus(friendId, userId, FriendshipStatus.CONFIRMED);

        log.debug("Friendship confirmed between user {} and user {}", userId, friendId);
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

        Set<User> friends = user.getFriends().stream()
                .map(Friendship::getUserId)
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.debug("User ID: {} has {} friends", id, friends.size());
        return friends;
    }

    public Set<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.getById(userId);
        User otherUser = userStorage.getById(otherUserId);

        if (user == null || otherUser == null) {
            throw new NotFoundObjectException("Объект не найден");
        }

        Set<Long> userConfirmedFriends = user.getFriends().stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getUserId)
                .collect(Collectors.toSet());

        Set<Long> otherUserConfirmedFriends = otherUser.getFriends().stream()
                .filter(friendship -> friendship.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getUserId)
                .collect(Collectors.toSet());

        Set<Long> commonFriendIds = userConfirmedFriends.stream()
                .filter(otherUserConfirmedFriends::contains)
                .collect(Collectors.toSet());

        return userStorage.findByIds(commonFriendIds);
    }
}
