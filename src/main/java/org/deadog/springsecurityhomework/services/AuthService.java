package org.deadog.springsecurityhomework.services;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.exceptions.RefreshTokenException;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.model.dto.AuthenticationRequest;
import org.deadog.springsecurityhomework.model.dto.AuthenticationResponse;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.deadog.springsecurityhomework.security.ReactiveAuthenticationManagementImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ReactiveAuthenticationManagementImpl authenticationManagement;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final RefreshTokenService refreshTokenService;

    public Mono<AuthenticationResponse> login(AuthenticationRequest request) {
        Mono<Authentication> authentication = authenticationManagement.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        return authentication.flatMap(auth -> {
            return userRepository.findByUsername(auth.getPrincipal().toString());
        }).flatMap(user -> {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Mono.error(new RuntimeException("Invalid password"));
            }

            String accessToken = tokenService.generateAccessToken(
                    user.getUsername(),
                    String.valueOf(user.getId()),
                    user.getRoles());
            String refreshTokenValue = tokenService.generateRefreshToken(user.getUsername());
            return refreshTokenService.save(user.getId(), refreshTokenValue)
                    .map(refreshToken -> new AuthenticationResponse(accessToken, refreshTokenValue));
        });
    }

    public Mono<AuthenticationResponse> refresh(String refreshTokenValue) {
        return Mono.just(refreshTokenValue).flatMap(ignore -> {
            if (tokenService.validateRefreshToken(refreshTokenValue)) {
                return refreshTokenService.getByValue(refreshTokenValue)
                        .switchIfEmpty(Mono.error(new RefreshTokenException("Token Not Found: " + refreshTokenValue)))
                        .flatMap(refreshToken -> userRepository.findById(refreshToken.getUserId()))
                        .switchIfEmpty(Mono.error(new RefreshTokenException("User not found: " + refreshTokenValue)))
                        .flatMap(user -> {
                            String accessToken = tokenService.generateAccessToken(
                                    user.getUsername(),
                                    String.valueOf(user.getId()),
                                    user.getRoles());
                            String newRefreshTokenValue = tokenService.generateRefreshToken(user.getUsername());
                            return refreshTokenService.save(user.getId(), newRefreshTokenValue)
                                    .map(refreshToken -> new AuthenticationResponse(accessToken, newRefreshTokenValue));
                        });
            }
            return Mono.error(new RefreshTokenException("Invalid refresh token: " + refreshTokenValue));
        });
    }
}
