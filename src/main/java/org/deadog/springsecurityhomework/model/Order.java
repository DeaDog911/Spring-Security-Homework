package org.deadog.springsecurityhomework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    private Long id;

    private String description;

    private String status;

    private Long userId;
}