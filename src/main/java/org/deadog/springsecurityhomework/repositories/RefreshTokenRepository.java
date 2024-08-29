package org.deadog.springsecurityhomework.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deadog.springsecurityhomework.exceptions.RefreshTokenException;
import org.deadog.springsecurityhomework.model.RefreshToken;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class RefreshTokenRepository {
    private static final String REFRESH_TOKEN_INDEX = "refreshTokenIndex";

    private final ReactiveValueOperations<String, RefreshToken> operationsForValue;

    private final ReactiveHashOperations<String, String, String> operationsForHash;

    public RefreshTokenRepository(ReactiveRedisTemplate<String, RefreshToken> refreshTokenReactiveRedisTemplate) {
        this.operationsForValue = refreshTokenReactiveRedisTemplate.opsForValue();
        this.operationsForHash = refreshTokenReactiveRedisTemplate.opsForHash();
    }

    public Mono<Boolean> save(RefreshToken refreshToken, Duration extTime) {
        return operationsForValue.set(refreshToken.getId(), refreshToken, extTime)
                .then(operationsForHash.put(REFRESH_TOKEN_INDEX, refreshToken.getValue(), refreshToken.getId()));
    }

    public Mono<RefreshToken> getByValue(String refreshToken) {
        return operationsForHash.get(REFRESH_TOKEN_INDEX, refreshToken)
                .flatMap(refreshTokenId -> operationsForHash.remove(REFRESH_TOKEN_INDEX, refreshToken)
                .flatMap(cleanupCount -> operationsForValue.get(refreshTokenId))
                .switchIfEmpty(Mono.error(new RefreshTokenException("Refresh token not found: " + refreshToken))));
    }

}
