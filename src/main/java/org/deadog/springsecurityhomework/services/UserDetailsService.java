package org.deadog.springsecurityhomework.services;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.model.AppUserDetails;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserDetailsService {
    private final UserRepository userRepository;

    public Mono<UserDetails> loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found"))).map(AppUserDetails::new);
    }
}
