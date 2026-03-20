package com.inventory.catalog.controller;

import com.inventory.catalog.dto.ProductResponse;
import com.inventory.catalog.dto.StockAdjustmentRequest;
import com.inventory.catalog.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/products")
public class InternalCatalogController {
    private final ProductService productService;

    public InternalCatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<ProductResponse> listAll() {
        return productService.list(null);
    }

    @GetMapping("/{id}")
    public Mono<ProductResponse> getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PatchMapping("/{id}/stock")
    public Mono<ProductResponse> adjustStock(@PathVariable UUID id, @Valid @RequestBody StockAdjustmentRequest request) {
        return productService.adjustStock(id, request.delta());
    }
}
