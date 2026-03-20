package com.inventory.inventory.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CatalogProductResponse(
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
