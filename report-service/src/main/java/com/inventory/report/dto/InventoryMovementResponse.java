package com.inventory.report.dto;

import com.inventory.report.model.ReportType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryMovementResponse(
        UUID id,
        UUID productId,
        String productName,
        String sku,
        ReportType type,
        Integer quantity,
        OffsetDateTime occurredAt,
        String reference,
        String notes,
        UUID createdByUserId,
        String createdByEmail,
        OffsetDateTime createdAt
) {
}
