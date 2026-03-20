package com.inventory.auth.controller;

import com.inventory.auth.dto.TokenValidationResponse;
import com.inventory.auth.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {
    private final AuthService authService;

    public InternalAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/validate")
    public Mono<TokenValidationResponse> validate(@RequestHeader("Authorization") String authorization) {
        return authService.validate(authorization);
    }
}
