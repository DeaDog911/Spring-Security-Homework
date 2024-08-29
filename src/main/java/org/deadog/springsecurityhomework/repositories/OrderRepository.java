package org.deadog.springsecurityhomework.repositories;

import org.deadog.springsecurityhomework.model.Order;
import org.deadog.springsecurityhomework.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAllByUserId(Long userId);
}
