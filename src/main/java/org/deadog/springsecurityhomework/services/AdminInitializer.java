package org.deadog.springsecurityhomework.services;

import lombok.RequiredArgsConstructor;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.model.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        userService.findByUsername("admin")
                .switchIfEmpty(Mono.defer(() -> {
                    User newAdmin = new User();
                    newAdmin.setUsername("admin");
                    newAdmin.setPassword("admin");
                    newAdmin.setRoles(Set.of(RoleType.ADMIN));
                    return userService.save(newAdmin);
                }))
                .subscribe();
    }
}