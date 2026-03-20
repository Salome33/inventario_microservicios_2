package com.inventory.report.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(Exception.class)
    public Mono<Map<String, Object>> handleUnexpected(Exception ex) {
        var body = new LinkedHashMap<String, Object>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", ex.getMessage());
        return Mono.just(body);
    }
}
