package com.inventory.auth.dto;

import com.inventory.auth.model.UserRole;
import com.inventory.auth.model.UserStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        UserStatus status,
        OffsetDateTime createdAt
) {
}
