package ru.yandex.practicum.filmorate.exeption;

public class NotFoundObjectException extends RuntimeException{
    public NotFoundObjectException(String message) {
        super(message);
    }
}

