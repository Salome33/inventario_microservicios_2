package com.inventory.catalog.repository;

import com.inventory.catalog.model.Category;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveCrudRepository<Category, UUID> {
    Mono<Category> findByNameIgnoreCase(String name);
}
