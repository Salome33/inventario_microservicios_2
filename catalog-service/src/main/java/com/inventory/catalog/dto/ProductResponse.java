package com.inventory.catalog.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String sku,
        String description,
        UUID categoryId,
        String categoryName,
        BigDecimal price,
        Integer stock,
        Boolean active,
        OffsetDateTime createdAt
) {
}
