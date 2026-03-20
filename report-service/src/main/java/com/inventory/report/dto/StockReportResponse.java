package com.inventory.report.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StockReportResponse(
        UUID auditId,
        OffsetDateTime generatedAt,
        String generatedByEmail,
        Integer totalRecords,
        List<CatalogProductResponse> rows
) {
}
