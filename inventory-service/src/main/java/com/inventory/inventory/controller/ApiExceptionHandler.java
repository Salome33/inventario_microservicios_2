package com.inventory.inventory.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        var body = new LinkedHashMap<String, Object>();
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason());
        return Mono.just(body);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<Map<String, Object>> handleValidation(WebExchangeBindException ex) {
        var errors = ex.getFieldErrors().stream()
                .collect(LinkedHashMap::new, (map, error) -> map.put(error.getField(), message(error)), Map::putAll);
        var body = new LinkedHashMap<String, Object>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Datos invalidos");
        body.put("fields", errors);
        return Mono.just(body);
    }

    private String message(FieldError error) {
        return error.getDefaultMessage() == null ? "Valor invalido" : error.getDefaultMessage();
    }
}
