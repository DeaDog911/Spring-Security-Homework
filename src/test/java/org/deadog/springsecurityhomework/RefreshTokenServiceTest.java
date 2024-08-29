package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.model.RefreshToken;
import org.deadog.springsecurityhomework.repositories.RefreshTokenRepository;
import org.deadog.springsecurityhomework.services.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private Duration refreshTokenExpiration;
    private RefreshToken testRefreshToken;
    private Long userId;
    private String tokenValue;

    @BeforeEach
    void setUp() {
        userId = 1L;
        tokenValue = UUID.randomUUID().toString();
        refreshTokenExpiration = Duration.ofDays(30);
        testRefreshToken = new RefreshToken(UUID.randomUUID().toString(), userId, tokenValue);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, refreshTokenExpiration.toString());
    }

    @Test
    void testSaveSuccess() {
        when(refreshTokenRepository.save(any(RefreshToken.class), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(refreshTokenService.save(userId, tokenValue))
                .expectNextMatches(savedToken -> savedToken.getUserId().equals(userId) &&
                        savedToken.getValue().equals(tokenValue))
                .verifyComplete();
    }

    @Test
    void testSaveFailure() {
        when(refreshTokenRepository.save(any(RefreshToken.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        StepVerifier.create(refreshTokenService.save(userId, tokenValue))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Failed to save refresh token for userId: " + userId))
                .verify();
    }

    @Test
    void testGetByValueSuccess() {
        when(refreshTokenRepository.getByValue(tokenValue)).thenReturn(Mono.just(testRefreshToken));

        StepVerifier.create(refreshTokenService.getByValue(tokenValue))
                .expectNext(testRefreshToken)
                .verifyComplete();
    }

    @Test
    void testGetByValueNotFound() {
        when(refreshTokenRepository.getByValue(tokenValue)).thenReturn(Mono.empty());

        StepVerifier.create(refreshTokenService.getByValue(tokenValue))
                .expectNextCount(0)
                .verifyComplete();
    }
}