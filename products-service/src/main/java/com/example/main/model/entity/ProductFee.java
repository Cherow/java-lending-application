package com.example.main.model.entity;


import com.example.main.model.enums.ApplicationStage;
import com.example.main.model.enums.CalculationType;
import com.example.main.model.enums.FeeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String feeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeeType feeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalculationType calculationType;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStage applicationStage;

    private Integer daysAfterDue;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}