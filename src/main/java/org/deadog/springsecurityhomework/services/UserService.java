package org.deadog.springsecurityhomework.services;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.model.dto.UserResponse;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public Mono<UserResponse> save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user)
                .flatMap(userResponse -> Mono.just(new UserResponse(userResponse)));
    }

    public Flux<UserResponse> findAll() {
        return userRepository.findAll()
                .flatMap(userResponse -> Mono.just(new UserResponse(userResponse)));
    }

    public Mono<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(userResponse -> Mono.just(new UserResponse(userResponse)));
    }

}
