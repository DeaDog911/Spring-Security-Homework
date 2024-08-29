package org.deadog.springsecurityhomework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {
    private String id;

    private Long userId;

    private String value;
}
