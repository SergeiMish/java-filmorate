package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;

public class UserDtoMapper {

    public static UserDto toDto (User model){
    return UserDto.builder().id(model.getId())
            .email(model.getEmail())
            .login(model.getLogin())
            .name(model.getName())
            .birthday(model.getBirthday())
            .build();
    }

    public static User toModel(UserDto userDto){
    return User.builder()
            .id(userDto.getId())
            .email(userDto.getEmail())
            .login(userDto.getLogin())
            .name(userDto.getName())
            .birthday(userDto.getBirthday())
            .build();
    }
}
