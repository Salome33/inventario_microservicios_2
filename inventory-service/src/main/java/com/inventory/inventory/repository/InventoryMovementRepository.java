package com.inventory.inventory.repository;

import com.inventory.inventory.model.InventoryMovement;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface InventoryMovementRepository extends ReactiveCrudRepository<InventoryMovement, UUID> {
}
