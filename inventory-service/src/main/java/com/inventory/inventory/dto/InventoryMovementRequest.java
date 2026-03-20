package com.inventory.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryMovementRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity,
        OffsetDateTime occurredAt,
        String reference,
        String notes
) {
}
