package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    public UserService(InMemoryUserStorage inMemoryUserStorage) {
    }

    public User addFriend(User user){

    }

    public User removeFromFriends (User user){

    }

    public Set<User> listFriends (){
        return listFriends();
    }
}
