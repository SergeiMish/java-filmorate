package ru.yandex.practicum.filmorate.exeption;

public class MpaNotFoundException extends RuntimeException {
    public MpaNotFoundException(Long id) {
        super("MPA с ID " + id + " не найден.");
    }
}
