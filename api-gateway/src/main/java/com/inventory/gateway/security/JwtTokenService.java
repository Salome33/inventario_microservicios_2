package com.inventory.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final SecretKey key;

    public JwtTokenService(JwtProperties properties) {
        this.key = parseKey(properties.secret());
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey parseKey(String configuredSecret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(configuredSecret));
        } catch (Exception ignored) {
            return Keys.hmacShaKeyFor(configuredSecret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
