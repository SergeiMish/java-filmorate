package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {

    private long id;
    @NotBlank
    private String name;
    @NotBlank
    @Length(max = 200)
    private String description;
    @NotNull
    @PastOrPresent
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Set<Long> likes;

    public int getLikesCount() {
        return likes.size();
    }
}
