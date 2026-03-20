package com.inventory.catalog.controller;

import com.inventory.catalog.dto.ProductRequest;
import com.inventory.catalog.dto.ProductResponse;
import com.inventory.catalog.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<ProductResponse> list(@RequestParam(required = false) UUID categoryId) {
        return service.list(categoryId);
    }

    @GetMapping("/{id}")
    public Mono<ProductResponse> get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public Mono<ProductResponse> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody ProductRequest request
    ) {
        return service.create(role, request);
    }

    @PutMapping("/{id}")
    public Mono<ProductResponse> update(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        return service.update(role, id, request);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@RequestHeader("X-User-Role") String role, @PathVariable UUID id) {
        return service.delete(role, id);
    }
}
