package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.mapper.EventDtoMapper;
import ru.yandex.practicum.filmorate.interfaces.EventStorage;
import ru.yandex.practicum.filmorate.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EventDao implements EventStorage {

    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper = new EventRowMapper();

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO Events (timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType(),
                event.getOperation(),
                event.getEntityId());
    }

    @Override
    public List<EventDto> getEventsByUserId(long userId) {
        String sql = "SELECT * FROM Events WHERE user_id = ? ORDER BY timestamp";
        EventRowMapper eventRowMapper = new EventRowMapper();
        List<Event> events = jdbcTemplate.query(sql, eventRowMapper, userId);
        return events.stream()
                .map(EventDtoMapper::toDto)
                .collect(Collectors.toList());
    }
}