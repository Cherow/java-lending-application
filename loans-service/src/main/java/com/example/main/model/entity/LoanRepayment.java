package com.example.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.example.main.model.enums.RepaymentStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 100)
    private String paymentReference;

    @Column(nullable = false, length = 50)
    private String paymentChannel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime paymentDate;
}