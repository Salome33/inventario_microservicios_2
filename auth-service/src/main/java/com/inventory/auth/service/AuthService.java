package com.inventory.auth.service;

import com.inventory.auth.config.JwtProperties;
import com.inventory.auth.dto.AuthResponse;
import com.inventory.auth.dto.CreateUserRequest;
import com.inventory.auth.dto.LoginRequest;
import com.inventory.auth.dto.RegisterRequest;
import com.inventory.auth.dto.TokenValidationResponse;
import com.inventory.auth.dto.UserProfileResponse;
import com.inventory.auth.model.RefreshToken;
import com.inventory.auth.model.UserAccount;
import com.inventory.auth.model.UserRole;
import com.inventory.auth.model.UserStatus;
import com.inventory.auth.repository.RefreshTokenRepository;
import com.inventory.auth.repository.UserAccountRepository;
import com.inventory.auth.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private final UserAccountRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserAccountRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return userRepository.count()
                .flatMap(totalUsers -> createUserInternal(
                        request.name(),
                        request.email(),
                        request.password(),
                        totalUsers == 0 ? UserRole.ADMIN : UserRole.USER
                ))
                .flatMap(this::newTokenPair);
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(normalizeEmail(request.email()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas")))
                .flatMap(user -> {
                    if (user.getStatus() != UserStatus.ACTIVE) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo"));
                    }
                    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
                    }
                    return newTokenPair(user);
                });
    }

    public Mono<AuthResponse> refresh(String refreshTokenPlain) {
        var hashed = hashToken(refreshTokenPlain);
        return refreshTokenRepository.findByTokenHashAndRevokedFalse(hashed)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalido")))
                .flatMap(token -> {
                    if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
                        token.setRevoked(true);
                        return refreshTokenRepository.save(token)
                                .then(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado")));
                    }
                    return userRepository.findById(token.getUserId())
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado")))
                            .flatMap(user -> {
                                if (user.getStatus() != UserStatus.ACTIVE) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo"));
                                }
                                token.setRevoked(true);
                                return refreshTokenRepository.save(token).then(newTokenPair(user));
                            });
                });
    }

    public Mono<UserProfileResponse> me(String bearerToken) {
        return Mono.fromSupplier(() -> jwtService.extractUserId(extractToken(bearerToken)))
                .flatMap(userRepository::findById)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido")))
                .map(this::toProfile);
    }

    public Mono<TokenValidationResponse> validate(String bearerToken) {
        return Mono.fromSupplier(() -> {
                    var claims = jwtService.parse(extractToken(bearerToken));
                    return new TokenValidationResponse(
                            true,
                            UUID.fromString(claims.getSubject()),
                            claims.get("email", String.class),
                            claims.get("role", String.class)
                    );
                })
                .onErrorReturn(new TokenValidationResponse(false, null, null, null));
    }

    public Flux<UserProfileResponse> listUsers(String currentRole) {
        assertAdmin(currentRole);
        return userRepository.findAll().map(this::toProfile);
    }

    public Mono<UserProfileResponse> createUser(String currentRole, CreateUserRequest request) {
        assertAdmin(currentRole);
        return createUserInternal(request.name(), request.email(), request.password(), request.role())
                .map(this::toProfile);
    }

    public Mono<UserProfileResponse> updateUserRole(String currentRole, UUID userId, UserRole role) {
        assertAdmin(currentRole);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .flatMap(user -> {
                    user.setRole(role);
                    return userRepository.save(user);
                })
                .map(this::toProfile);
    }

    public Mono<UserProfileResponse> updateUserStatus(String currentRole, UUID userId, UserStatus status) {
        assertAdmin(currentRole);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .flatMap(user -> {
                    user.setStatus(status);
                    return userRepository.save(user);
                })
                .map(this::toProfile);
    }

    private Mono<UserAccount> createUserInternal(String name, String email, String password, UserRole role) {
        var normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .flatMap(existing -> Mono.<UserAccount>error(new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado")))
                .switchIfEmpty(Mono.defer(() -> {
                    var user = new UserAccount();
                    user.setName(name.trim());
                    user.setEmail(normalizedEmail);
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setRole(role == null ? UserRole.USER : role);
                    user.setStatus(UserStatus.ACTIVE);
                    user.setCreatedAt(OffsetDateTime.now());
                    return userRepository.save(user);
                }));
    }

    private Mono<AuthResponse> newTokenPair(UserAccount user) {
        var accessToken = jwtService.generateAccessToken(user);
        var refreshPlain = UUID.randomUUID() + "." + UUID.randomUUID();
        var refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hashToken(refreshPlain));
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(jwtProperties.refreshTokenDays()));
        refreshToken.setCreatedAt(OffsetDateTime.now());
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken)
                .thenReturn(new AuthResponse(accessToken, refreshPlain, "Bearer", jwtService.accessTokenTtlSeconds()));
    }

    private void assertAdmin(String role) {
        if (!UserRole.ADMIN.name().equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede realizar esta accion");
        }
    }

    private UserProfileResponse toProfile(UserAccount user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase().trim();
    }

    private String hashToken(String plain) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var bytes = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo hashear el token", ex);
        }
    }

    private String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Bearer requerido");
        }
        return bearerToken.substring(7);
    }
}
