package com.inventory.inventory.controller;

import com.inventory.inventory.dto.InventoryMovementResponse;
import com.inventory.inventory.model.MovementType;
import com.inventory.inventory.service.InventoryMovementService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/internal/movements")
public class InternalInventoryController {
    private final InventoryMovementService service;

    public InternalInventoryController(InventoryMovementService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<InventoryMovementResponse> list(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return service.list(productId, type, from, to);
    }
}
