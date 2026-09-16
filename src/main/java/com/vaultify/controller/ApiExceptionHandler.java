package com.vaultify.controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "Requisição inválida", details));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status,
                exception.getReason() == null ? "Erro na requisição" : exception.getReason(), List.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> illegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, exception.getMessage(), List.of()));
    }

    public record ApiError(Instant timestamp, int status, String message, List<String> details) { }
}