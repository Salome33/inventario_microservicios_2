package com.inventory.catalog.repository;

import com.inventory.catalog.model.Product;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<Product, UUID> {
    Flux<Product> findByCategoryId(UUID categoryId);

    Mono<Product> findBySkuIgnoreCase(String sku);
}
