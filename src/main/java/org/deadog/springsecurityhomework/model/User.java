package org.deadog.springsecurityhomework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User  {
    @Id
    private Long id;

    private String username;

    private String password;

    private String email;

    private Set<RoleType> roles = new HashSet<>();
}
