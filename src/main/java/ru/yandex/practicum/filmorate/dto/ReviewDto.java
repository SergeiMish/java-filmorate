package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class ReviewDto {

    @NotNull
    Integer reviewId;

    @NotNull
    Integer filmId;

    @NotNull
    Integer userId;

    @NotNull
    @JsonProperty("isPositive")
    Boolean isPositive;

    int useful;

    @NotBlank
    @Size(max = 400, message = "Content is too long")
    String content;
}
