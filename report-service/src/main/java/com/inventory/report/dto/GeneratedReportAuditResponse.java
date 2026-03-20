package com.inventory.report.dto;

import com.inventory.report.model.ReportType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GeneratedReportAuditResponse(
        UUID id,
        ReportType reportType,
        String generatedByEmail,
        String filters,
        Integer totalRecords,
        OffsetDateTime createdAt
) {
}
