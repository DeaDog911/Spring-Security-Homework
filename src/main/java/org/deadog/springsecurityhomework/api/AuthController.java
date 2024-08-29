package org.deadog.springsecurityhomework.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.exceptions.ApplicationException;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.model.dto.AuthenticationRequest;
import org.deadog.springsecurityhomework.model.dto.AuthenticationResponse;
import org.deadog.springsecurityhomework.model.dto.RefreshTokenRequest;
import org.deadog.springsecurityhomework.model.dto.RegisterUserRequest;
import org.deadog.springsecurityhomework.services.AuthService;
import org.deadog.springsecurityhomework.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth Controller", description = "Контроллер аутентификации")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/registration")
    @Operation(summary = "Регистрация нового пользователя", description = "Регистрация пользователя с передачей учетных данных", tags = {"Auth Controller"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка регистрации", content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Mono<ResponseEntity<String>> registration(@RequestBody RegisterUserRequest userRequest) {
        return Mono.just(userRequest).flatMap(
                request -> {
                    if (!request.getPassword().equals(request.getPasswordConfirm())) {
                        return Mono.error(new ApplicationException("Пароли не совпадают"));
                    }
                    return Mono.just(request);
                }
        ).flatMap(request -> {
            User userToCreate = new User();
            userToCreate.setUsername(request.getUsername());
            userToCreate.setPassword(request.getPassword());
            userToCreate.setEmail(request.getEmail());
            userToCreate.setRoles(Collections.singleton(RoleType.USER));
            return userService.save(userToCreate)
                    .map(user -> ResponseEntity.ok("User created"));
        }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя", description = "Авторизация пользователя с использованием учетных данных", tags = {"Auth Controller"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аутентификация успешна", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неправильные учетные данные", content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Mono<ResponseEntity<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Получение нового токена обновления", description = "Обновление JWT токена с использованием существующего refresh токена", tags = {"Auth Controller"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Новый токен успешно создан", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе", content = @Content(schema = @Schema(implementation = String.class)))
    })
    public Mono<ResponseEntity<AuthenticationResponse>> getNewRefreshToken(@RequestBody RefreshTokenRequest refreshToken) {
        return authService.refresh(refreshToken.getRefreshToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)));
    }
}