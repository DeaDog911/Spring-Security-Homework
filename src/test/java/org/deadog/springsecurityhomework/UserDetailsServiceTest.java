package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.deadog.springsecurityhomework.services.UserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserDetailsServiceTest {

    @MockBean
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User(1L, "testuser", "password", "email", Set.of(RoleType.USER));
        userDetailsService = new UserDetailsService(userRepository);
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));

        StepVerifier.create(userDetailsService.loadUserByUsername("testuser"))
                .expectNextMatches(userDetails -> userDetails.getUsername().equals(testUser.getUsername()))
                .verifyComplete();
    }

    @Test
    void testLoadUserByUsernameUserNotFound() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Mono.empty());

        StepVerifier.create(userDetailsService.loadUserByUsername("unknownuser"))
                .expectErrorMatches(throwable -> throwable instanceof UsernameNotFoundException)
                .verify();
    }
}
