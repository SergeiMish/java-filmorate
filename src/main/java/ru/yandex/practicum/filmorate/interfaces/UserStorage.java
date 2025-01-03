package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {

    User create(User user);

    User delete(User user);

    User update(User user);

    User getById(Long id);

    Collection<User> getAll();

    Set<User> findByIds(Set<Long> ids);
}
