package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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


    public void addFriend(Long id, Long friendId) {
        User user = inMemoryUserStorage.getUserById(id);
        User friend = inMemoryUserStorage.getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(id);
            inMemoryUserStorage.updateUser(user);
            inMemoryUserStorage.updateUser(friend);
        }
    }

    public void removeFromFriends (Long id, Long friendId) {
        User user = inMemoryUserStorage.getUserById(id);
        User friend = inMemoryUserStorage.getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(id);
            inMemoryUserStorage.updateUser(user);
            inMemoryUserStorage.updateUser(friend);
        }
    }

    public Set<User> listFriends(Long id) {
        User user = inMemoryUserStorage.getUserById(id);
        if (user != null) {
            return user.getFriends().stream()
                    .map(inMemoryUserStorage::getUserById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
}
