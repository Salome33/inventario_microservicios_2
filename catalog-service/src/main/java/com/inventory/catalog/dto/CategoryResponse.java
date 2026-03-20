package com.inventory.catalog.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        OffsetDateTime createdAt
) {
}
