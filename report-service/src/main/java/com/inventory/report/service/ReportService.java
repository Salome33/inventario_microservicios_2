package com.inventory.report.service;

import com.inventory.report.dto.CatalogProductResponse;
import com.inventory.report.dto.GeneratedReportAuditResponse;
import com.inventory.report.dto.InventoryMovementResponse;
import com.inventory.report.dto.MovementReportResponse;
import com.inventory.report.dto.StockReportResponse;
import com.inventory.report.model.GeneratedReportAudit;
import com.inventory.report.model.ReportType;
import com.inventory.report.repository.GeneratedReportAuditRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReportService {
    private final GeneratedReportAuditRepository auditRepository;
    private final WebClient webClient;
    private final String internalToken;
    private final String catalogServiceUrl;
    private final String inventoryServiceUrl;

    public ReportService(
            GeneratedReportAuditRepository auditRepository,
            WebClient.Builder webClientBuilder,
            @Value("${security.internal-token}") String internalToken,
            @Value("${security.catalog-service-url}") String catalogServiceUrl,
            @Value("${security.inventory-service-url}") String inventoryServiceUrl
    ) {
        this.auditRepository = auditRepository;
        this.webClient = webClientBuilder.build();
        this.internalToken = internalToken;
        this.catalogServiceUrl = catalogServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    public Mono<StockReportResponse> generateStockReport(String role, String email) {
        assertAdmin(role);
        return fetchProducts()
                .collectList()
                .flatMap(rows -> saveAudit(ReportType.STOCK, email, "stock-report", rows.size())
                        .map(audit -> new StockReportResponse(
                                audit.getId(),
                                audit.getCreatedAt(),
                                email,
                                rows.size(),
                                rows
                        )));
    }

    public Mono<MovementReportResponse> generateMovementReport(
            String role,
            String email,
            UUID productId,
            ReportType type,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        assertAdmin(role);
        var movementType = type == null || type == ReportType.STOCK || type == ReportType.MOVEMENT ? null : type;
        return fetchMovements(productId, movementType, from, to)
                .collectList()
                .flatMap(rows -> saveAudit(type == null ? ReportType.MOVEMENT : type, email, filters(productId, type, from, to), rows.size())
                        .map(audit -> new MovementReportResponse(
                                audit.getId(),
                                audit.getCreatedAt(),
                                email,
                                productId,
                                type == null ? "ALL" : type.name(),
                                from,
                                to,
                                rows.size(),
                                rows
                        )));
    }

    public Flux<GeneratedReportAuditResponse> listAudits(String role) {
        assertAdmin(role);
        return auditRepository.findAll()
                .map(audit -> new GeneratedReportAuditResponse(
                        audit.getId(),
                        audit.getReportType(),
                        audit.getGeneratedByEmail(),
                        audit.getFilters(),
                        audit.getTotalRecords(),
                        audit.getCreatedAt()
                ));
    }

    private Flux<CatalogProductResponse> fetchProducts() {
        return webClient.get()
                .uri(catalogServiceUrl + "/internal/products")
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .bodyToFlux(CatalogProductResponse.class)
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No fue posible generar el reporte de stock"));
    }

    private Flux<InventoryMovementResponse> fetchMovements(UUID productId, ReportType type, OffsetDateTime from, OffsetDateTime to) {
        var builder = UriComponentsBuilder.fromUriString(inventoryServiceUrl + "/internal/movements");
        if (productId != null) {
            builder.queryParam("productId", productId);
        }
        if (type != null) {
            builder.queryParam("type", type.name());
        }
        if (from != null) {
            builder.queryParam("from", from);
        }
        if (to != null) {
            builder.queryParam("to", to);
        }
        return webClient.get()
                .uri(builder.build(true).toUri())
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .bodyToFlux(InventoryMovementResponse.class)
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No fue posible generar el reporte de movimientos"));
    }

    private Mono<GeneratedReportAudit> saveAudit(ReportType reportType, String email, String filters, int totalRecords) {
        var audit = new GeneratedReportAudit();
        audit.setReportType(reportType);
        audit.setGeneratedByEmail(email);
        audit.setFilters(filters);
        audit.setTotalRecords(totalRecords);
        audit.setCreatedAt(OffsetDateTime.now());
        return auditRepository.save(audit);
    }

    private String filters(UUID productId, ReportType type, OffsetDateTime from, OffsetDateTime to) {
        return "productId=" + productId + ", type=" + type + ", from=" + from + ", to=" + to;
    }

    private void assertAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede consultar reportes");
        }
    }
}
