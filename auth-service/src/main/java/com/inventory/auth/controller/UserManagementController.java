package com.inventory.auth.controller;

import com.inventory.auth.dto.CreateUserRequest;
import com.inventory.auth.dto.UpdateUserRoleRequest;
import com.inventory.auth.dto.UpdateUserStatusRequest;
import com.inventory.auth.dto.UserProfileResponse;
import com.inventory.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {
    private final AuthService authService;

    public UserManagementController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public Flux<UserProfileResponse> list(@RequestHeader("X-User-Role") String role) {
        return authService.listUsers(role);
    }

    @PostMapping
    public Mono<UserProfileResponse> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return authService.createUser(role, request);
    }

    @PutMapping("/{id}/role")
    public Mono<UserProfileResponse> updateRole(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return authService.updateUserRole(role, id, request.role());
    }

    @PutMapping("/{id}/status")
    public Mono<UserProfileResponse> updateStatus(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return authService.updateUserStatus(role, id, request.status());
    }
}
