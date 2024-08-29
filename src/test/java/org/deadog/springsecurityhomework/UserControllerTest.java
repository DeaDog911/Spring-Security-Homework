package org.deadog.springsecurityhomework;

import org.deadog.springsecurityhomework.api.UserController;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.dto.UserResponse;
import org.deadog.springsecurityhomework.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username="admin", authorities = "ADMIN")
    public void testGetAll_Authorized() {
        UserResponse user = new UserResponse();
        user.setUsername("test");
        user.setEmail("test");
        user.setRoles(Set.of(RoleType.USER));

        when(userService.findAll()).thenReturn(Flux.just(user));

        webTestClient.get().uri("/api/users/getAll")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class).hasSize(1).contains(user);
    }

    @Test
    @WithAnonymousUser
    public void testGetAll_Unauthorized() {
        UserResponse user = new UserResponse();
        user.setUsername("test");
        user.setEmail("test");
        user.setRoles(Set.of(RoleType.USER));

        when(userService.findAll()).thenReturn(Flux.just(user));

        webTestClient.get().uri("/api/users/getAll")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
