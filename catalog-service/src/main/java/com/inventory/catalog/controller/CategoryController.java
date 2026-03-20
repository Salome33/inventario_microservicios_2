package com.inventory.catalog.controller;

import com.inventory.catalog.dto.CategoryRequest;
import com.inventory.catalog.dto.CategoryResponse;
import com.inventory.catalog.service.CategoryService;
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
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<CategoryResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Mono<CategoryResponse> get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public Mono<CategoryResponse> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CategoryRequest request
    ) {
        return service.create(role, request);
    }

    @PutMapping("/{id}")
    public Mono<CategoryResponse> update(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return service.update(role, id, request);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@RequestHeader("X-User-Role") String role, @PathVariable UUID id) {
        return service.delete(role, id);
    }
}
