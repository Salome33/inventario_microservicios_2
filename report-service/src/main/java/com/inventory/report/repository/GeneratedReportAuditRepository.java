package com.inventory.report.repository;

import com.inventory.report.model.GeneratedReportAudit;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface GeneratedReportAuditRepository extends ReactiveCrudRepository<GeneratedReportAudit, UUID> {
}
