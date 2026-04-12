package com.example.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxLimit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableLimit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal utilizedLimit;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}