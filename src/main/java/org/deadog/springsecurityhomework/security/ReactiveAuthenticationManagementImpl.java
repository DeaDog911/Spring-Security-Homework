package org.deadog.springsecurityhomework.security;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.exceptions.AuthException;
import org.deadog.springsecurityhomework.services.UserDetailsService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReactiveAuthenticationManagementImpl implements ReactiveAuthenticationManager {
    private final UserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return userDetailsService.loadUserByUsername((String) authentication.getPrincipal())
                .filter(UserDetails::isEnabled)
                .switchIfEmpty(Mono.error(new AuthException("User disabled")))
                .map(userDetails -> authentication);
    }
}
