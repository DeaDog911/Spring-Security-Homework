package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.api.AuthController;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.model.dto.*;
import org.deadog.springsecurityhomework.services.AuthService;
import org.deadog.springsecurityhomework.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AuthController.class, excludeAutoConfiguration = ReactiveSecurityAutoConfiguration.class)
public class AuthControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Test
    public void testAuthenticate_Success() {
        AuthenticationRequest request = new AuthenticationRequest("user", "password");
        AuthenticationResponse authResponse = new AuthenticationResponse("accessToken", "refreshToken");

        when(authService.login(request)).thenReturn(Mono.just(authResponse));

        webTestClient.mutateWith(csrf()).post().uri("/api/auth/login")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponse.class).isEqualTo(authResponse);
    }

    @Test
    public void testAuthenticate_Failure() {
        AuthenticationRequest request = new AuthenticationRequest("user", "password");

        when(authService.login(any(AuthenticationRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

        webTestClient.mutateWith(csrf()).post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void testGetNewRefreshToken_Failure() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalidRefreshToken");

        when(authService.refresh(any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Invalid refresh token")));

        webTestClient.mutateWith(csrf()).post().uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void testRegistration_Success() {
        RegisterUserRequest request = new RegisterUserRequest("newUser", "password", "password", "email@example.com");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRoles(Set.of(RoleType.USER));

        when(userService.save(user)).thenReturn(Mono.just(new UserResponse()));

        webTestClient.mutateWith(csrf()).post().uri("/api/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("User created");
    }

    @Test
    public void testRegistration_PasswordMismatch() {
        RegisterUserRequest request = new RegisterUserRequest("newUser", "password", "wrongPassword", "email@example.com");

        webTestClient.mutateWith(csrf()).post().uri("/api/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Пароли не совпадают");
    }

}

