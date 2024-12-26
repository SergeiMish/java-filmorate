package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {

    User create(User user);

    boolean delete(Long id);

    User update(User user);

    User getById(Long id);

    Collection<User> getAll();

    Set<User> findByIds(Set<Long> ids);

    void addFriendship(Long userId, Long friendId, FriendshipStatus status);

    void removeFriendship(Long userId, Long friendId);

    void updateFriendshipStatus(Long userId, Long friendId, FriendshipStatus confirmed);
}
