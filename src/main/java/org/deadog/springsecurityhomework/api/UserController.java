package org.deadog.springsecurityhomework.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.model.dto.UserResponse;
import org.deadog.springsecurityhomework.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Контроллер для работы с пользователями")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAll")
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей. Доступно только для пользователей с ролью ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль ADMIN."),
            @ApiResponse(responseCode = "401", description = "Неавторизован. Нужен JWT токен.")
    })
    public Flux<UserResponse> getAll() {
        return userService.findAll();
    }
}
