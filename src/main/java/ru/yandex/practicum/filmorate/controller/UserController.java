package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.Set;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final InMemoryUserStorage inMemoryUserStorage;
    private final UserService userService;

    public UserController(InMemoryUserStorage inMemoryUserStorage, UserService userService) {
        this.inMemoryUserStorage = inMemoryUserStorage;
        this.userService = userService;
    }

    @PostMapping
    public User postUser(@Valid @RequestBody User user) {
        return inMemoryUserStorage.createUser(user);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return inMemoryUserStorage.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return inMemoryUserStorage.getUserById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/friends")
    private Set<User> friendsLis(@PathVariable Long id) {
        return userService.listFriends(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriends(@PathVariable Long id,@PathVariable Long friendId){
        return userService.addFriend(id,friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriends(@PathVariable Long id,@PathVariable Long friendId){
        return userService.removeFromFriends(id, friendId);
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) {
        return inMemoryUserStorage.updateUser(user);
    }

}
