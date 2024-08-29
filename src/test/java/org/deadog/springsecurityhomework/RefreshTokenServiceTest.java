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

    private RefreshTokenService refreshTokenService; // Automatically inject mocks

    private Duration refreshTokenExpiration;
    private RefreshToken testRefreshToken;
    private Long userId;
    private String tokenValue;

    @BeforeEach
    void setUp() {
        // Initialize test data
        userId = 1L;
        tokenValue = UUID.randomUUID().toString();
        refreshTokenExpiration = Duration.ofDays(30); // Example expiration duration
        testRefreshToken = new RefreshToken(UUID.randomUUID().toString(), userId, tokenValue);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, refreshTokenExpiration.toString());
    }

    @Test
    void testSaveSuccess() {
        // Mock repository save to return true, indicating successful save
        when(refreshTokenRepository.save(any(RefreshToken.class), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Testing save method
        StepVerifier.create(refreshTokenService.save(userId, tokenValue))
                .expectNextMatches(savedToken -> savedToken.getUserId().equals(userId) &&
                        savedToken.getValue().equals(tokenValue))
                .verifyComplete();
    }

    @Test
    void testSaveFailure() {
        when(refreshTokenRepository.save(any(RefreshToken.class), any(Duration.class)))
                .thenReturn(Mono.just(false));

        // Testing save method for failure scenario
        StepVerifier.create(refreshTokenService.save(userId, tokenValue))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Failed to save refresh token for userId: " + userId))
                .verify();
    }

    @Test
    void testGetByValueSuccess() {
        // Mock repository getByValue to return a valid refresh token
        when(refreshTokenRepository.getByValue(tokenValue)).thenReturn(Mono.just(testRefreshToken));

        // Testing getByValue method
        StepVerifier.create(refreshTokenService.getByValue(tokenValue))
                .expectNext(testRefreshToken)
                .verifyComplete();
    }

    @Test
    void testGetByValueNotFound() {
        // Mock repository getByValue to return empty, indicating token not found
        when(refreshTokenRepository.getByValue(tokenValue)).thenReturn(Mono.empty());

        // Testing getByValue method for not found scenario
        StepVerifier.create(refreshTokenService.getByValue(tokenValue))
                .expectNextCount(0)
                .verifyComplete();
    }
}