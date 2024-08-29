package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.model.Order;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.repositories.OrderRepository;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.deadog.springsecurityhomework.services.OrderService;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.ReactorContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private TestExecutionListener reactorContextTestExecutionListener =
            new ReactorContextTestExecutionListener();

    private OrderService orderService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        orderService = new OrderService(orderRepository, userRepository);

        when(authentication.getPrincipal()).thenReturn("testuser");

        TestSecurityContextHolder.setAuthentication(authentication);
        reactorContextTestExecutionListener.beforeTestMethod(null);
    }

    @After
    public void tearDown() throws Exception {
        reactorContextTestExecutionListener.afterTestMethod(null);
    }

    @Test
    public void findAllOrders_ShouldReturnOrders() {
        Order order = new Order();
        when(orderRepository.findAll()).thenReturn(Flux.just(order));

        StepVerifier.create(orderService.findAllOrders())
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    public void createOrder_ShouldSaveOrder() {
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        Order order = new Order();

        // Mock repository responses
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));  // Return Mono.just(user) instead of null
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.createOrder(order))
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    public void getUserOrders_ShouldReturnUserOrders() {
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        Order order = new Order();

        // Mock repository responses
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));  // Return Mono.just(user) instead of null
        when(orderRepository.findAllByUserId(user.getId())).thenReturn(Flux.just(order));

        StepVerifier.create(orderService.getUserOrders())
                .expectNext(order)
                .verifyComplete();
    }
}