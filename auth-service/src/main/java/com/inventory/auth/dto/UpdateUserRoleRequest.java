package com.inventory.auth.dto;

import com.inventory.auth.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull UserRole role) {
}
