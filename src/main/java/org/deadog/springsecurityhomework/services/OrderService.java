package org.deadog.springsecurityhomework.services;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.exceptions.ApplicationException;
import org.deadog.springsecurityhomework.model.Order;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.repositories.OrderRepository;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public Flux<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public Mono<Order> createOrder(Order order) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String username = authentication.getPrincipal().toString();
                    return userRepository.findByUsername(username)
                            .switchIfEmpty(Mono.error(new ApplicationException("User not found: " + username)));
                })
                .flatMap(user -> {
                    order.setUserId(user.getId());
                    return orderRepository.save(order);
                });
    }

    public Flux<Order> getUserOrders() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String username = authentication.getPrincipal().toString();
                    return userRepository.findByUsername(username)
                            .switchIfEmpty(Mono.error(new ApplicationException("User not found: " + username)));
                })
                .flatMapMany(user -> orderRepository.findAllByUserId(user.getId()));
    }
}
