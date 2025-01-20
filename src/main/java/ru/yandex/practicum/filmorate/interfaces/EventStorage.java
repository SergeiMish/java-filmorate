package ru.yandex.practicum.filmorate.interfaces;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    void addEvent(Event event);

    List<EventDto> getEventsByUserId(long userId);
}