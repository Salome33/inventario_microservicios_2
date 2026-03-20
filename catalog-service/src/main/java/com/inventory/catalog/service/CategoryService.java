package com.inventory.catalog.service;

import com.inventory.catalog.dto.CategoryRequest;
import com.inventory.catalog.dto.CategoryResponse;
import com.inventory.catalog.model.Category;
import com.inventory.catalog.repository.CategoryRepository;
import com.inventory.catalog.repository.ProductRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Flux<CategoryResponse> list() {
        return categoryRepository.findAll().map(this::toResponse);
    }

    public Mono<CategoryResponse> getById(UUID id) {
        return categoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada")))
                .map(this::toResponse);
    }

    public Mono<CategoryResponse> create(String role, CategoryRequest request) {
        assertAdmin(role);
        return categoryRepository.findByNameIgnoreCase(request.name().trim())
                .flatMap(existing -> Mono.<CategoryResponse>error(new ResponseStatusException(HttpStatus.CONFLICT, "La categoria ya existe")))
                .switchIfEmpty(Mono.defer(() -> {
                    var category = new Category();
                    category.setName(request.name().trim());
                    category.setDescription(request.description());
                    category.setCreatedAt(OffsetDateTime.now());
                    return categoryRepository.save(category).map(this::toResponse);
                }));
    }

    public Mono<CategoryResponse> update(String role, UUID id, CategoryRequest request) {
        assertAdmin(role);
        return categoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada")))
                .flatMap(category -> {
                    category.setName(request.name().trim());
                    category.setDescription(request.description());
                    return categoryRepository.save(category);
                })
                .map(this::toResponse);
    }

    public Mono<Void> delete(String role, UUID id) {
        assertAdmin(role);
        return productRepository.findByCategoryId(id)
                .hasElements()
                .flatMap(hasProducts -> {
                    if (hasProducts) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar una categoria con productos asociados"));
                    }
                    return categoryRepository.deleteById(id);
                });
    }

    private void assertAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede realizar esta accion");
        }
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getCreatedAt());
    }
}
