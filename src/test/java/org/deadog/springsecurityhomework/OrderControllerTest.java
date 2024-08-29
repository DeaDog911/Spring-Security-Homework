package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.api.OrderController;
import org.deadog.springsecurityhomework.model.Order;
import org.deadog.springsecurityhomework.services.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(OrderController.class)
public class OrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testGetAllOrders_AsAdmin() {
        Order order1 = new Order(1L, "Description 1", "NEW", 1L);
        Order order2 = new Order(2L, "Description 2", "NEW", 2L);

        when(orderService.findAllOrders()).thenReturn(Flux.just(order1, order2));

        webTestClient.get().uri("/api/orders/getAll")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Order.class).hasSize(2).contains(order1, order2);
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testCreateOrder_AsUser() {
        Order order = new Order(1L, "Description", "NEW", null);

        when(orderService.createOrder(order)).thenReturn(Mono.just(order));

        webTestClient.mutateWith(csrf()).post()
                .uri("/api/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class).isEqualTo(order);
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetMyOrders_AsUser() {
        Order order1 = new Order(1L, "Description 1", "NEW", 1L);
        Order order2 = new Order(2L, "Description 2", "NEW", 1L);

        when(orderService.getUserOrders()).thenReturn(Flux.just(order1, order2));

        webTestClient.get().uri("/api/orders/getMy")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Order.class).hasSize(2).contains(order1, order2);
    }

    @Test
    public void testGetMyOrders_Unauthorized() {
        webTestClient.get().uri("/api/orders/getMy")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}