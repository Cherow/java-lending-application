package com.example.main.model.dto.response;

import com.example.main.model.enums.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private String loanNumber;
    private Long customerId;
    private Long productId;
    private BigDecimal principalAmount;
    private BigDecimal disbursedAmount;
    private BigDecimal totalFees;
    private BigDecimal totalPaid;
    private BigDecimal balance;
    private Integer tenure;
    private LocalDate disbursementDate;
    private LocalDate dueDate;
    private LoanStatus status;
    private List<LoanFeeResponse> fees;
    private List<LoanRepaymentResponse> repayments;
}