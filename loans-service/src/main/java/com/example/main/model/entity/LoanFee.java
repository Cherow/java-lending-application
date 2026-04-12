package com.example.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.main.model.enums.LoanFeeType;

@Entity
@Table(name = "loan_fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanFeeType feeType;

    @Column(nullable = false, length = 100)
    private String feeName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @Column(length = 255)
    private String reason;
}