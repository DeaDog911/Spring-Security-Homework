package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.exceptions.ApplicationException;
import org.deadog.springsecurityhomework.model.Order;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.repositories.OrderRepository;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.deadog.springsecurityhomework.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "email", Set.of(RoleType.USER));
        testOrder = new Order(1L, "description", "status", testUser.getId());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser.getUsername());
    }

    @Test
    void testFindAllOrders() {
        when(orderRepository.findAll()).thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.findAllOrders())
                .expectNext(testOrder)
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testCreateOrderSuccess() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Mono.just(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));

        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));

        StepVerifier.create(orderService.createOrder(new Order()))
                .expectNextMatches(order -> order.getUserId().equals(testUser.getId()))
                .verifyComplete();

        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrderUserNotFound() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Mono.empty());

        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));

        StepVerifier.create(orderService.createOrder(new Order()))
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException &&
                        throwable.getMessage().equals("User not found: " + testUser.getUsername()))
                .verify();

        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetUserOrdersSuccess() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Mono.just(testUser));
        when(orderRepository.findAllByUserId(testUser.getId())).thenReturn(Flux.just(testOrder));

        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));

        StepVerifier.create(orderService.getUserOrders())
                .expectNext(testOrder)
                .verifyComplete();

        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        verify(orderRepository, times(1)).findAllByUserId(testUser.getId());
    }

    @Test
    void testGetUserOrdersUserNotFound() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Mono.empty());

        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));

        StepVerifier.create(orderService.getUserOrders())
                .expectErrorMatches(throwable -> throwable instanceof ApplicationException &&
                        throwable.getMessage().equals("User not found: " + testUser.getUsername()))
                .verify();

        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        verify(orderRepository, never()).findAllByUserId(anyLong());
    }
}
