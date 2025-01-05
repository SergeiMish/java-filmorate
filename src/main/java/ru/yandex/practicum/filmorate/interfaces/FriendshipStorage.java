package ru.yandex.practicum.filmorate.interfaces;

import java.util.List;

public interface FriendshipStorage {

    void addFriend(Long user1Id, Long user2Id);

    void removeFriend(Long id, Long friendId);

    boolean isFriendshipExists(Long user1Id, Long user2Id);

    List<Long> getFriendIds(Long id);
}
