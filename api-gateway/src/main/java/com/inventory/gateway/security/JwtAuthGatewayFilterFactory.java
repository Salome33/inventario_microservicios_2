package com.inventory.gateway.security;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {
    private final JwtTokenService jwtTokenService;
    private final String internalGatewayToken;

    public JwtAuthGatewayFilterFactory(
            JwtTokenService jwtTokenService,
            @Value("${security.internal-token}") String internalGatewayToken
    ) {
        super(Config.class);
        this.jwtTokenService = jwtTokenService;
        this.internalGatewayToken = internalGatewayToken;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Token JWT requerido");
            }

            var token = authHeader.substring(7);
            final Claims claims;
            try {
                claims = jwtTokenService.parse(token);
            } catch (Exception ex) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Token JWT invalido");
            }

            var request = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Email", claims.get("email", String.class))
                    .header("X-User-Role", claims.get("role", String.class))
                    .header("X-Internal-Token", internalGatewayToken)
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of();
    }

    public static class Config {
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var body = ("{\"status\":" + status.value() + ",\"error\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        var buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
