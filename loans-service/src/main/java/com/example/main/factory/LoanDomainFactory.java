package com.example.main.factory;

import com.example.main.model.dto.request.CreateLoanRequest;
import com.example.main.model.dto.request.RepayLoanRequest;
import com.example.main.model.dto.response.ProductResponse;
import com.example.main.model.entity.Loan;
import com.example.main.model.entity.LoanRepayment;
import com.example.main.model.enums.LoanStatus;
import com.example.main.model.enums.RepaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class LoanDomainFactory {

    public Loan buildLoan(CreateLoanRequest request, ProductResponse product) {
        return Loan.builder()
                .loanNumber(generateLoanNumber())
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .principalAmount(request.getPrincipalAmount())
                .disbursedAmount(BigDecimal.ZERO)
                .totalFees(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .balance(request.getPrincipalAmount())
                .tenure(request.getTenure())
                .disbursementDate(null)
                .dueDate(resolveDueDate(LocalDate.now(), request.getTenure(), product))
                .status(LoanStatus.CREATED)
                .build();
    }

    public LoanRepayment buildLoanRepayment(Loan loan, RepayLoanRequest request) {
        return LoanRepayment.builder()
                .loan(loan)
                .amountPaid(request.getAmount())
                .paymentReference(request.getPaymentReference())
                .paymentChannel(request.getPaymentChannel())
                .status(RepaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    private String generateLoanNumber() {
        return "LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LocalDate resolveDueDate(LocalDate baseDate, Integer tenure, ProductResponse product) {
        return switch (product.getTenureType()) {
            case DAYS -> baseDate.plusDays(tenure);
            case MONTHS -> baseDate.plusMonths(tenure);
        };
    }
}