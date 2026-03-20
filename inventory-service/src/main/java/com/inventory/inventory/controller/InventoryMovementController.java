package com.inventory.inventory.controller;

import com.inventory.inventory.dto.InventoryMovementRequest;
import com.inventory.inventory.dto.InventoryMovementResponse;
import com.inventory.inventory.model.MovementType;
import com.inventory.inventory.service.InventoryMovementService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/inventory")
public class InventoryMovementController {
    private final InventoryMovementService service;

    public InventoryMovementController(InventoryMovementService service) {
        this.service = service;
    }

    @GetMapping("/movements")
    public Flux<InventoryMovementResponse> list(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return service.list(productId, type, from, to);
    }

    @GetMapping("/movements/{id}")
    public Mono<InventoryMovementResponse> get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping("/entries")
    public Mono<InventoryMovementResponse> createEntry(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody InventoryMovementRequest request
    ) {
        return service.createMovement(userId, email, MovementType.ENTRY, request);
    }

    @PostMapping("/exits")
    public Mono<InventoryMovementResponse> createExit(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody InventoryMovementRequest request
    ) {
        return service.createMovement(userId, email, MovementType.EXIT, request);
    }
}
