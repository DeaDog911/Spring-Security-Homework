package org.deadog.springsecurityhomework.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.model.Order;
import org.deadog.springsecurityhomework.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name="Order Controller", description = "Контроллер для работы с заказами")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OrderController {
    private final OrderService orderService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAll")
    @Operation(
            summary = "Получить все заказы",
            description = "Возвращает список всех заказов. Доступно только для пользователей с ролью ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль ADMIN."),
            @ApiResponse(responseCode = "401", description = "Неавторизован. Нужен JWT токен.")
    })
    public Flux<Order> getAllOrders() {
        return orderService.findAllOrders();
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/create")
    @Operation(
            summary = "Создать заказ",
            description = "Создает новый заказ от имени текущего пользователя. Доступно для пользователей с ролью USER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно создан", content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль USER."),
            @ApiResponse(responseCode = "401", description = "Неавторизован. Нужен JWT токен.")
    })
    public Mono<ResponseEntity<Order>> createOrder(@RequestBody Order order) {
        return orderService.createOrder(order)
                .map(ResponseEntity::ok)
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/getMy")
    @Operation(
            summary = "Получить свои заказы",
            description = "Возвращает заказы, принадлежащие текущему аутентифицированному пользователю. Доступно для пользователей с ролью USER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов пользователя успешно получен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуется роль USER."),
            @ApiResponse(responseCode = "401", description = "Неавторизован. Нужен JWT токен.")
    })
    public Flux<Order> getMyOrders() {
        return orderService.getUserOrders();
    }
}
