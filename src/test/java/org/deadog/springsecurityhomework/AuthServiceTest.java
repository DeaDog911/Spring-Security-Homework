package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.exceptions.RefreshTokenException;
import org.deadog.springsecurityhomework.model.RefreshToken;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.model.dto.AuthenticationRequest;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.deadog.springsecurityhomework.security.ReactiveAuthenticationManagementImpl;
import org.deadog.springsecurityhomework.services.AuthService;
import org.deadog.springsecurityhomework.services.RefreshTokenService;
import org.deadog.springsecurityhomework.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @Mock
    private ReactiveAuthenticationManagementImpl authenticationManagement;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    private AuthenticationRequest authenticationRequest;
    private User testUser;
    private String accessToken = "accessToken";
    private String refreshTokenValue = "refreshTokenValue";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        authenticationRequest = new AuthenticationRequest("testuser", "password");
        testUser = new User(1L, "testuser", "encodedPassword", "email",  Set.of(RoleType.USER));
        authService = new AuthService(authenticationManagement, userRepository, passwordEncoder, tokenService, refreshTokenService);
    }

    @Test
    void testLoginSuccess() {
        // Mocking the authentication management to return a valid authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        when(authenticationManagement.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(authentication));

        // Mocking user repository to return a valid user
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        // Mocking password encoder to match passwords
        when(passwordEncoder.matches(eq("password"), eq("encodedPassword"))).thenReturn(true);

        // Mocking token service to generate access and refresh tokens
        when(tokenService.generateAccessToken(anyString(), anyString(), anySet())).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(anyString())).thenReturn(refreshTokenValue);

        // Mocking refresh token service to save refresh token
        when(refreshTokenService.save(eq(1L), eq(refreshTokenValue)))
                .thenReturn(Mono.just(new RefreshToken(UUID.randomUUID().toString(), 1L, refreshTokenValue)));

        // Testing the login method
        StepVerifier.create(authService.login(authenticationRequest))
                .expectNextMatches(response -> response.getToken().equals(accessToken)
                        && response.getRefreshToken().equals(refreshTokenValue))
                .verifyComplete();
    }

    @Test
    void testLoginInvalidPassword() {
        // Mocking authentication management to return a valid authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        when(authenticationManagement.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(authentication));

        // Mocking user repository to return a valid user
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        // Mocking password encoder to not match passwords
        when(passwordEncoder.matches(eq("password"), eq("encodedPassword"))).thenReturn(false);

        // Testing the login method for invalid password scenario
        StepVerifier.create(authService.login(authenticationRequest))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Invalid password"))
                .verify();
    }

    @Test
    void testRefreshSuccess() {
        // Mocking token service to validate refresh token
        when(tokenService.validateRefreshToken(refreshTokenValue)).thenReturn(true);

        // Mocking refresh token service to return a refresh token
        when(refreshTokenService.getByValue(refreshTokenValue))
                .thenReturn(Mono.just(new RefreshToken(UUID.randomUUID().toString(), 1L, refreshTokenValue)));

        // Mocking user repository to return a valid user
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        // Mocking token service to generate access and refresh tokens
        when(tokenService.generateAccessToken(anyString(), anyString(), anySet())).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(anyString())).thenReturn(refreshTokenValue);

        // Mocking refresh token service to save refresh token
        when(refreshTokenService.save(eq(1L), eq(refreshTokenValue)))
                .thenReturn(Mono.just(new RefreshToken(UUID.randomUUID().toString(), 1L, refreshTokenValue)));

        // Testing the refresh method
        StepVerifier.create(authService.refresh(refreshTokenValue))
                .expectNextMatches(response -> response.getToken().equals(accessToken)
                        && response.getRefreshToken().equals(refreshTokenValue))
                .verifyComplete();
    }

    @Test
    void testRefreshInvalidToken() {
        // Mocking token service to invalidate refresh token
        when(tokenService.validateRefreshToken(refreshTokenValue)).thenReturn(false);

        // Testing the refresh method for invalid token scenario
        StepVerifier.create(authService.refresh(refreshTokenValue))
                .expectErrorMatches(throwable -> throwable instanceof RefreshTokenException &&
                        throwable.getMessage().equals("Invalid refresh token: " + refreshTokenValue))
                .verify();
    }
}
