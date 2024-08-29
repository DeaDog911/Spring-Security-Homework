package org.deadog.springsecurityhomework.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;

    private String refreshToken;
}
