package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.filmorate.exeption.NotFoundObjectException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    private static final String ERROR = "error";

    private static final String MESSAGE = "message";

    @ExceptionHandler(NotFoundObjectException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundObjectException ex) {
        log.debug("Not found error: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR, "Not Found");
        errorResponse.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
        log.debug("Validation error: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR, "Validation doesn't pass");
        errorResponse.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.debug("Unexpected error: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR, "Unexpected error");
        errorResponse.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
