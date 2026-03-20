package com.inventory.report.controller;

import com.inventory.report.dto.GeneratedReportAuditResponse;
import com.inventory.report.dto.MovementReportResponse;
import com.inventory.report.dto.StockReportResponse;
import com.inventory.report.model.ReportType;
import com.inventory.report.service.ReportService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/stock")
    public Mono<StockReportResponse> stockReport(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email
    ) {
        return reportService.generateStockReport(role, email);
    }

    @GetMapping("/movements")
    public Mono<MovementReportResponse> movementReport(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return reportService.generateMovementReport(role, email, productId, type, from, to);
    }

    @GetMapping("/audits")
    public Flux<GeneratedReportAuditResponse> audits(@RequestHeader("X-User-Role") String role) {
        return reportService.listAudits(role);
    }
}
