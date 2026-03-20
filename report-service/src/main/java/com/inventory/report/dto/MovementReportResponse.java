package com.inventory.report.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MovementReportResponse(
        UUID auditId,
        OffsetDateTime generatedAt,
        String generatedByEmail,
        UUID productId,
        String type,
        OffsetDateTime from,
        OffsetDateTime to,
        Integer totalRecords,
        List<InventoryMovementResponse> rows
) {
}
