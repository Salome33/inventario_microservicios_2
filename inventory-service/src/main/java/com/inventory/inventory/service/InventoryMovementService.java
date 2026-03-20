package com.inventory.inventory.service;

import com.inventory.inventory.dto.CatalogProductResponse;
import com.inventory.inventory.dto.InventoryMovementRequest;
import com.inventory.inventory.dto.InventoryMovementResponse;
import com.inventory.inventory.dto.StockAdjustmentRequest;
import com.inventory.inventory.model.InventoryMovement;
import com.inventory.inventory.model.MovementType;
import com.inventory.inventory.repository.InventoryMovementRepository;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class InventoryMovementService {
    private final InventoryMovementRepository repository;
    private final WebClient webClient;
    private final String internalToken;
    private final String catalogServiceUrl;

    public InventoryMovementService(
            InventoryMovementRepository repository,
            WebClient.Builder webClientBuilder,
            @Value("${security.internal-token}") String internalToken,
            @Value("${security.catalog-service-url}") String catalogServiceUrl
    ) {
        this.repository = repository;
        this.webClient = webClientBuilder.build();
        this.internalToken = internalToken;
        this.catalogServiceUrl = catalogServiceUrl;
    }

    public Flux<InventoryMovementResponse> list(UUID productId, MovementType type, OffsetDateTime from, OffsetDateTime to) {
        return repository.findAll()
                .filter(movement -> productId == null || movement.getProductId().equals(productId))
                .filter(movement -> type == null || movement.getType() == type)
                .filter(movement -> from == null || !movement.getOccurredAt().isBefore(from))
                .filter(movement -> to == null || !movement.getOccurredAt().isAfter(to))
                .flatMap(this::toResponse)
                .sort(Comparator.comparing(InventoryMovementResponse::occurredAt).reversed());
    }

    public Mono<InventoryMovementResponse> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimiento no encontrado")))
                .flatMap(this::toResponse);
    }

    public Mono<InventoryMovementResponse> createMovement(
            String userId,
            String userEmail,
            MovementType type,
            InventoryMovementRequest request
    ) {
        return fetchProduct(request.productId())
                .flatMap(product -> validateStock(product, type, request.quantity()))
                .flatMap(product -> adjustStock(product.id(), type == MovementType.ENTRY ? request.quantity() : -request.quantity())
                        .flatMap(updatedProduct -> saveMovement(userId, userEmail, type, request)
                                .onErrorResume(error -> adjustStock(product.id(), type == MovementType.ENTRY ? -request.quantity() : request.quantity())
                                        .then(Mono.error(error)))
                                .flatMap(saved -> toResponse(saved, updatedProduct))));
    }

    private Mono<CatalogProductResponse> validateStock(CatalogProductResponse product, MovementType type, Integer quantity) {
        if (Boolean.FALSE.equals(product.active())) {
            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "No se pueden registrar movimientos para un producto inactivo"));
        }
        if (type == MovementType.EXIT && product.stock() < quantity) {
            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para registrar la salida"));
        }
        return Mono.just(product);
    }

    private Mono<InventoryMovement> saveMovement(
            String userId,
            String userEmail,
            MovementType type,
            InventoryMovementRequest request
    ) {
        var movement = new InventoryMovement();
        movement.setProductId(request.productId());
        movement.setType(type);
        movement.setQuantity(request.quantity());
        movement.setOccurredAt(request.occurredAt() == null ? OffsetDateTime.now() : request.occurredAt());
        movement.setReference(request.reference());
        movement.setNotes(request.notes());
        movement.setCreatedByUserId(UUID.fromString(userId));
        movement.setCreatedByEmail(userEmail);
        movement.setCreatedAt(OffsetDateTime.now());
        return repository.save(movement);
    }

    private Mono<InventoryMovementResponse> toResponse(InventoryMovement movement) {
        return fetchProduct(movement.getProductId()).flatMap(product -> toResponse(movement, product));
    }

    private Mono<InventoryMovementResponse> toResponse(InventoryMovement movement, CatalogProductResponse product) {
        return Mono.just(new InventoryMovementResponse(
                movement.getId(),
                movement.getProductId(),
                product.name(),
                product.sku(),
                movement.getType(),
                movement.getQuantity(),
                movement.getOccurredAt(),
                movement.getReference(),
                movement.getNotes(),
                movement.getCreatedByUserId(),
                movement.getCreatedByEmail(),
                movement.getCreatedAt()
        ));
    }

    private Mono<CatalogProductResponse> fetchProduct(UUID productId) {
        return webClient.get()
                .uri(catalogServiceUrl + "/internal/products/{id}", productId)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .bodyToMono(CatalogProductResponse.class)
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No fue posible consultar el catalogo de productos"));
    }

    private Mono<CatalogProductResponse> adjustStock(UUID productId, int delta) {
        return webClient.patch()
                .uri(catalogServiceUrl + "/internal/products/{id}/stock", productId)
                .header("X-Internal-Token", internalToken)
                .bodyValue(new StockAdjustmentRequest(delta))
                .retrieve()
                .bodyToMono(CatalogProductResponse.class)
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No fue posible actualizar el stock del producto"));
    }
}
