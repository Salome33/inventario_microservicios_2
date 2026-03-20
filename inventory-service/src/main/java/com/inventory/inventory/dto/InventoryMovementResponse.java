package com.inventory.inventory.dto;

import com.inventory.inventory.model.MovementType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryMovementResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        MovementType type,
        Integer quantity,
        OffsetDateTime occurredAt,
        String reference,
        String notes,
        UUID createdByUserId,
        String createdByEmail,
        OffsetDateTime createdAt
) {
}
