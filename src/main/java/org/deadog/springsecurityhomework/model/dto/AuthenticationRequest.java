package org.deadog.springsecurityhomework.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;

@Data
@AllArgsConstructor
public class AuthenticationRequest {
    private String username;

    private String password;
}
