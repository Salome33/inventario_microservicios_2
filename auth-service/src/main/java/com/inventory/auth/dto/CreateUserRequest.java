package com.inventory.auth.dto;

import com.inventory.auth.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(min = 8, max = 72) String password,
        UserRole role
) {
}
