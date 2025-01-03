package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MpaDto {

    private Long id;

    @NotBlank(message = "Название MPA не может быть пустым")
    private String name;
}
