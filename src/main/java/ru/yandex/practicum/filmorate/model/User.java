package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
    private final Set<Friendship> friends = new HashSet<>();
    private long id;
    @Email(message = "Некорректный email")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;
    private String name;
    @NotNull(message = "Дата рождения не может быть null")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public boolean addFriend(Long friendId) {
        return friends.add(new Friendship(friendId, FriendshipStatus.UNCONFIRMED));
    }

    public boolean confirmFriend(Long friendId) {
        for (Friendship friendship : friends) {
            if (friendship.getUserId().equals(friendId) && friendship.getStatus() == FriendshipStatus.UNCONFIRMED) {
                friendship.setStatus(FriendshipStatus.CONFIRMED);
                return true;
            }
        }
        return false;
    }

    public boolean removeFriend(Long friendId) {
        return friends.removeIf(friendship -> friendship.getUserId().equals(friendId));
    }
}



