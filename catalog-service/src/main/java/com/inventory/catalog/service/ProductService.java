package com.inventory.catalog.service;

import com.inventory.catalog.dto.ProductRequest;
import com.inventory.catalog.dto.ProductResponse;
import com.inventory.catalog.model.Product;
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
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Flux<ProductResponse> list(UUID categoryId) {
        var products = categoryId == null ? productRepository.findAll() : productRepository.findByCategoryId(categoryId);
        return products.flatMap(this::toResponse);
    }

    public Mono<ProductResponse> getById(UUID id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado")))
                .flatMap(this::toResponse);
    }

    public Mono<ProductResponse> create(String role, ProductRequest request) {
        assertAdmin(role);
        return categoryRepository.findById(request.categoryId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoria no existe")))
                .then(productRepository.findBySkuIgnoreCase(request.sku().trim()))
                .flatMap(existing -> Mono.<ProductResponse>error(new ResponseStatusException(HttpStatus.CONFLICT, "El SKU ya existe")))
                .switchIfEmpty(Mono.defer(() -> {
                    var product = new Product();
                    fillProduct(product, request);
                    product.setCreatedAt(OffsetDateTime.now());
                    return productRepository.save(product).flatMap(this::toResponse);
                }));
    }

    public Mono<ProductResponse> update(String role, UUID id, ProductRequest request) {
        assertAdmin(role);
        return categoryRepository.findById(request.categoryId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoria no existe")))
                .then(productRepository.findById(id))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado")))
                .flatMap(product -> {
                    fillProduct(product, request);
                    return productRepository.save(product);
                })
                .flatMap(this::toResponse);
    }

    public Mono<Void> delete(String role, UUID id) {
        assertAdmin(role);
        return productRepository.deleteById(id);
    }

    public Mono<ProductResponse> adjustStock(UUID id, int delta) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado")))
                .flatMap(product -> {
                    var newStock = product.getStock() + delta;
                    if (newStock < 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para la salida solicitada"));
                    }
                    product.setStock(newStock);
                    return productRepository.save(product);
                })
                .flatMap(this::toResponse);
    }

    private void fillProduct(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setSku(request.sku().trim().toUpperCase());
        product.setDescription(request.description());
        product.setCategoryId(request.categoryId());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(request.active() == null ? Boolean.TRUE : request.active());
    }

    private void assertAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede realizar esta accion");
        }
    }

    private Mono<ProductResponse> toResponse(Product product) {
        return categoryRepository.findById(product.getCategoryId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria asociada no encontrada")))
                .map(category -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getDescription(),
                        product.getCategoryId(),
                        category.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getActive(),
                        product.getCreatedAt()
                ));
    }
}
