package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

public class EventDtoMapper {

    public static EventDto toDto(Event event) {
        return EventDto.builder()
                .eventId(event.getEventId())
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .entityId(event.getEntityId())
                .build();
    }

    public static Event toModel(EventDto eventDto) {
        return Event.builder()
                .eventId(eventDto.getEventId())
                .timestamp(eventDto.getTimestamp())
                .userId(eventDto.getUserId())
                .eventType(eventDto.getEventType())
                .operation(eventDto.getOperation())
                .entityId(eventDto.getEntityId())
                .build();
    }
}
