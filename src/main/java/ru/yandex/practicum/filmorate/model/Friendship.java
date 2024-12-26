package ru.yandex.practicum.filmorate.model;

public class Friendship {
    private final Long userId;
    private FriendshipStatus status;

    public Friendship(Long userId, FriendshipStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }
}

