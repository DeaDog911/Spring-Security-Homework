package org.deadog.springsecurityhomework.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {
    private String username;

    private String password;

    private String passwordConfirm;

    private String email;
}
