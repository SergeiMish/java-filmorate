package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.mapper.UserDtoMapper;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @PostMapping
    public UserDto postUser(@RequestBody @Valid UserDto userDto) {
        User user = UserDtoMapper.toModel(userDto);

        User createdUser = userStorage.create(user);

        return UserDtoMapper.toDto(createdUser);
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userStorage.getAll().stream()
                .map(UserDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean isDeleted = userStorage.delete(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userStorage.getById(id);
        if (user != null) {
            return ResponseEntity.ok(UserDtoMapper.toDto(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .map(UserDtoMapper::toDto)
                .collect(Collectors.toSet());
    }

    @GetMapping("/{id}/friends")
    public Set<UserDto> getFriends(@PathVariable Long id) {
        return userService.listFriends(id).stream()
                .map(UserDtoMapper::toDto)
                .collect(Collectors.toSet());
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDto> addFriends(@PathVariable Long id, @PathVariable Long friendId) {
        User user = userService.addFriend(id, friendId);
        if (user == null) {
            throw new RuntimeException("User is null after adding friend");
        }
        return ResponseEntity.ok(UserDtoMapper.toDto(user));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<UserDto> deleteFriends(@PathVariable Long id, @PathVariable Long friendId) {
        User user = userService.removeFriend(id, friendId);
        return ResponseEntity.ok(UserDtoMapper.toDto(user));
    }

    @PutMapping
    public ResponseEntity<UserDto> putUser(@RequestBody @Valid UserDto userDto) {
        User user = UserDtoMapper.toModel(userDto);
        User updatedUser = userStorage.update(user);
        return ResponseEntity.ok(UserDtoMapper.toDto(updatedUser));
    }
}
