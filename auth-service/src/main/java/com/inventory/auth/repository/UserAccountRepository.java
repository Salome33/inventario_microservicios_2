package com.inventory.auth.repository;

import com.inventory.auth.model.UserAccount;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserAccountRepository extends ReactiveCrudRepository<UserAccount, UUID> {
    Mono<UserAccount> findByEmail(String email);
}
