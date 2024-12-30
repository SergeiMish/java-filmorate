package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Mpa {

    private Long id;

    @NotBlank(message = "Название MPA не может быть пустым")
    private String name;
    public Mpa() {
        this.id = id;
        this.name = name;
    }
}
