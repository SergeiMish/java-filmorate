package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User deleteUser(User user) {
        return null;
    }

    public Collection<User> getUser() {
        return users.values();
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь не найден");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    private long getNextId() {
        return users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}
