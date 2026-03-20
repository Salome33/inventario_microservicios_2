package com.inventory.auth.security;

import com.inventory.auth.config.JwtProperties;
import com.inventory.auth.model.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = parseKey(properties.secret());
    }

    public String generateAccessToken(UserAccount user) {
        var now = Instant.now();
        var expiresAt = now.plusSeconds(properties.accessTokenMinutes() * 60);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parse(token).getSubject());
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenMinutes() * 60;
    }

    private SecretKey parseKey(String configuredSecret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(configuredSecret));
        } catch (Exception ignored) {
            return Keys.hmacShaKeyFor(configuredSecret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
