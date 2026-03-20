package com.inventory.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotBlank String name,
        @NotBlank String sku,
        String description,
        @NotNull UUID categoryId,
        @NotNull @Min(0) Integer stock,
        @NotNull @Min(0) BigDecimal price,
        Boolean active
) {
}
