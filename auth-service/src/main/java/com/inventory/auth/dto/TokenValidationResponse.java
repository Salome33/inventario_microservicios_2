package com.inventory.auth.dto;

import java.util.UUID;

public record TokenValidationResponse(
        boolean valid,
        UUID userId,
        String email,
        String role
) {
}
