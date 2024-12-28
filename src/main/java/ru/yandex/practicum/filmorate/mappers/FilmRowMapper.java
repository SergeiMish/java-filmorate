package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        String mpaRatingName = rs.getString("mpa_rating");
        MpaRating mpaRating = null;

        if (mpaRatingName != null) {
            try {
                mpaRating = MpaRating.valueOf(mpaRatingName);
            } catch (IllegalArgumentException e) {
                throw new SQLException("Некорректное значение MPA Rating: " + mpaRatingName, e);
            }
        } else {
            throw new SQLException("MPA Rating не может быть null");
        }

        return Film.builder().id(rs.getLong(1))
                .name(rs.getString(2))
                .description(rs.getString(3))
                .releaseDate(rs.getTimestamp(4).toLocalDateTime().toLocalDate())
                .duration(rs.getInt(5))
                .mpaRating(MpaRating.valueOf(rs.getString(6)))
                .likes(new HashSet<>())
                .genres(new ArrayList<>())
                .build();
    }
}
