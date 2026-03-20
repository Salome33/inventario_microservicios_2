package com.inventory.auth.repository;

import com.inventory.auth.model.RefreshToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, java.util.UUID> {
    Mono<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
