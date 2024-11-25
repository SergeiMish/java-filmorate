package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Collection;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final InMemoryUserStorage inMemoryUserStorage;

    public UserController(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    @PostMapping
    public User postUser(@Valid @RequestBody User user) {
        return inMemoryUserStorage.createUser(user);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return inMemoryUserStorage.getAllUsers();
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) {
        return inMemoryUserStorage.updateUser(user);
    }

}
