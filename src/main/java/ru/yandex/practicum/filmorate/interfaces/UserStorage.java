package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User create(User user);

    boolean delete(Long id);

    User update(User user);

    User getById(Long id);

    Collection<User> getAll();

}
