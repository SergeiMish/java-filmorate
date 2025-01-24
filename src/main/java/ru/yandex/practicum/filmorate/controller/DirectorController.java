package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.mapper.DirectorDtoMapper;
import ru.yandex.practicum.filmorate.interfaces.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorStorage directorStorage;

    // Получение всех режиссёров
    @GetMapping
    public Collection<DirectorDto> getAllDirectors() {
        return directorStorage.getAll().stream()
                              .map(DirectorDtoMapper::toDto)
                              .collect(Collectors.toList());
    }

    // Получение режиссёра по ID
    @GetMapping("/{id}")
    public DirectorDto getDirectorById(@PathVariable Long id) {
        Director director = directorStorage.getById(id);
        return DirectorDtoMapper.toDto(director);
    }

    // Создание нового режиссёра
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto createDirector(@RequestBody @Valid DirectorDto directorDto) {
        try {
        Director director = DirectorDtoMapper.toEntity(directorDto);
        Director createdDirector = directorStorage.create(director);
        return DirectorDtoMapper.toDto(createdDirector);
        } catch (Exception e) {
            // Логируем ошибку и возвращаем подробное сообщение
            log.error("Error while creating director", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Произошла непредвиденная ошибка.", e);
        }
        }

    // Обновление информации о режиссёре
    @PutMapping("/{id}")
    public DirectorDto updateDirector(@PathVariable Long id, @RequestBody @Valid DirectorDto directorDto) {
        Director director = DirectorDtoMapper.toEntity(directorDto);
        director.setId(id);  // Устанавливаем ID из URL в переданный объект
        Director updatedDirector = directorStorage.update(director);
        return DirectorDtoMapper.toDto(updatedDirector);
    }

    // Удаление режиссёра
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable Long id) {
        directorStorage.delete(id);
    }
}