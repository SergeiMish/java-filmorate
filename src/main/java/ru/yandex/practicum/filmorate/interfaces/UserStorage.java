package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {

    User createUser(User user);

    User deleteUser(User user);

    User updateUser(User user);
}
