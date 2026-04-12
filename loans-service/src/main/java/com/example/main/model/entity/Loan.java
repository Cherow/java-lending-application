package com.example.main.model.entity;

import com.example.main.model.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loanNumber;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalFees;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPaid;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private Integer tenure;


    private LocalDate disbursementDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoanRepayment> repayments = new ArrayList<>();

    @OneToMany(  mappedBy= "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoanFee> fees = new ArrayList<>();

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


