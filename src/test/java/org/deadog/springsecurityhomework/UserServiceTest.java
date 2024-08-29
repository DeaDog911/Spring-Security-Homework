package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.deadog.springsecurityhomework.repositories.UserRepository;
import org.deadog.springsecurityhomework.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User(1L, "testuser", "password", "email", Set.of(RoleType.USER));

        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testSaveUserSuccess() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.save(testUser))
                .expectNextMatches(userResponse -> userResponse.getUsername().equals(testUser.getUsername()))
                .verifyComplete();
    }

    @Test
    void testFindAllUsersSuccess() {
        when(userRepository.findAll()).thenReturn(Flux.just(testUser));

        StepVerifier.create(userService.findAll())
                .expectNextMatches(userResponse -> userResponse.getUsername().equals(testUser.getUsername()))
                .verifyComplete();
    }

    @Test
    void testFindByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findByUsername("testuser"))
                .expectNextMatches(userResponse -> userResponse.getUsername().equals(testUser.getUsername()))
                .verifyComplete();
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Mono.empty());

        StepVerifier.create(userService.findByUsername("unknownuser"))
                .expectNextCount(0)
                .verifyComplete();
    }
}
