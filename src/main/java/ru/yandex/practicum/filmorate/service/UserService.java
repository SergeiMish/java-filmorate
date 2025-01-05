package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.FriendshipStorage;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public User addFriend(Long user1Id, Long user2Id) {
        if (Objects.equals(user1Id, user2Id)) {
            log.error("Нельзя добавить в друзья самого себя");
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }

        User mainUser = getUserOrThrow(user1Id);
        getUserOrThrow(user2Id);

        if (!friendshipStorage.isFriendshipExists(user1Id, user2Id)) {
            friendshipStorage.addFriend(user1Id, user2Id);
            log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", user1Id, user2Id);
        } else {
            log.info("Пользователь с id = {} уже является другом пользователя с id = {}", user1Id, user2Id);
        }

        return mainUser;
    }

    public User removeFriend(Long id, Long friendId) {
        getUserOrThrow(id);
        getUserOrThrow(friendId);

        friendshipStorage.removeFriend(id, friendId);

        log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", id, friendId);

        return getUserOrThrow(id);
    }

    public User getUserOrThrow(Long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundObjectException("ID " + id + " не найден");
        }
        return user;
    }

    public Set<User> listFriends(Long id) {
        getUserOrThrow(id);

        List<Long> friendIds = friendshipStorage.getFriendIds(id);
        Set<User> friends = friendIds.stream()
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.debug("User ID: {} has {} friends", id, friends.size());
        return friends;
    }

    public Set<User> getCommonFriends(Long userId, Long otherUserId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherUserId);

        List<Long> userFriendIds = friendshipStorage.getFriendIds(userId);
        List<Long> otherUserFriendIds = friendshipStorage.getFriendIds(otherUserId);

        Set<Long> commonFriendIds = userFriendIds.stream()
                .filter(otherUserFriendIds::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(userStorage::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}