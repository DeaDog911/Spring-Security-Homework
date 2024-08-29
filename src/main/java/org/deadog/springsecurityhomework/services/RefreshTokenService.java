package org.deadog.springsecurityhomework.services;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.model.RefreshToken;
import org.deadog.springsecurityhomework.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    private final Duration refreshTokenExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.expiration.refresh}") String refreshTokenExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = Duration.parse(refreshTokenExpiration);
    }

    public Mono<RefreshToken> save(Long userId, String value) {
        String id = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken(id, userId, value);

        return refreshTokenRepository.save(refreshToken, refreshTokenExpiration)
                .filter(isSuccess -> isSuccess)
                .flatMap(igonre -> Mono.just(refreshToken))
                .switchIfEmpty(Mono.error(new RuntimeException("Failed to save refresh token for userId: " + userId)));
    }

    public Mono<RefreshToken> getByValue(String value) {
        return refreshTokenRepository.getByValue(value);
    }
}
