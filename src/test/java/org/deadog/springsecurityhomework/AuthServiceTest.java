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

        authenticationRequest = new AuthenticationRequest("testuser", "password");
        testUser = new User(1L, "testuser", "encodedPassword", "email",  Set.of(RoleType.USER));
        authService = new AuthService(authenticationManagement, userRepository, passwordEncoder, tokenService, refreshTokenService);
    }

    @Test
    void testLoginSuccess() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        when(authenticationManagement.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(authentication));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        when(passwordEncoder.matches(eq("password"), eq("encodedPassword"))).thenReturn(true);

        when(tokenService.generateAccessToken(anyString(), anyString(), anySet())).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(anyString())).thenReturn(refreshTokenValue);

        when(refreshTokenService.save(eq(1L), eq(refreshTokenValue)))
                .thenReturn(Mono.just(new RefreshToken(UUID.randomUUID().toString(), 1L, refreshTokenValue)));

        StepVerifier.create(authService.login(authenticationRequest))
                .expectNextMatches(response -> response.getToken().equals(accessToken)
                        && response.getRefreshToken().equals(refreshTokenValue))
                .verifyComplete();
    }

    @Test
    void testLoginInvalidPassword() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", "password");
        when(authenticationManagement.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(authentication));

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        when(passwordEncoder.matches(eq("password"), eq("encodedPassword"))).thenReturn(false);

        StepVerifier.create(authService.login(authenticationRequest))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Invalid password"))
                .verify();
    }

    @Test
    void testRefreshSuccess() {
        when(tokenService.validateRefreshToken(refreshTokenValue)).thenReturn(true);

        when(refreshTokenService.getByValue(refreshTokenValue))
                .thenReturn(Mono.just(new RefreshToken(UUID.randomUUID().toString(), 1L, refreshTokenValue)));

        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        when(tokenService.generateAccessToken(anyString(), anyString(), anySet())).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(anyString())).thenReturn(refreshTokenValue);

        when(refreshTokenService.save(eq(1L), eq(refreshTokenValue)))
                .thenReturn(Mono.just(new RefreshToken(UUID.randomUUID().toString(), 1L, refreshTokenValue)));

        StepVerifier.create(authService.refresh(refreshTokenValue))
                .expectNextMatches(response -> response.getToken().equals(accessToken)
                        && response.getRefreshToken().equals(refreshTokenValue))
                .verifyComplete();
    }

    @Test
    void testRefreshInvalidToken() {
        when(tokenService.validateRefreshToken(refreshTokenValue)).thenReturn(false);

        StepVerifier.create(authService.refresh(refreshTokenValue))
                .expectErrorMatches(throwable -> throwable instanceof RefreshTokenException &&
                        throwable.getMessage().equals("Invalid refresh token: " + refreshTokenValue))
                .verify();
    }
}
