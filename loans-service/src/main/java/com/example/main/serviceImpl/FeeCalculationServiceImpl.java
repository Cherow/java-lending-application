package com.example.main.serviceImpl;

import com.example.main.model.entity.Loan;
import com.example.main.model.entity.LoanFee;
import com.example.main.model.enums.LoanFeeType;
import com.example.main.repository.LoanFeeRepository;
import com.example.main.service.FeeCalculationService;
import com.example.main.model.dto.response.ProductFeeResponse;
import com.example.main.model.dto.response.ProductResponse;
import com.example.main.model.enums.ApplicationStage;
import com.example.main.model.enums.CalculationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeeCalculationServiceImpl implements FeeCalculationService {

    private final LoanFeeRepository loanFeeRepository;

    @Override
    public BigDecimal applyOriginationFees(Loan loan, ProductResponse product) {
        return applyFeesByStage(loan, product, ApplicationStage.ORIGINATION, "Origination fee applied");
    }

    @Override
    public BigDecimal applyLateFees(Loan loan, ProductResponse product) {
        return applyFeesByStage(loan, product, ApplicationStage.LATE_PAYMENT, "Late fee applied");
    }

    @Override
    public BigDecimal applyDailyFees(Loan loan, ProductResponse product) {
        return applyFeesByStage(loan, product, ApplicationStage.DAILY_ACCRUAL, "Daily fee applied");
    }

    private BigDecimal applyFeesByStage(Loan loan, ProductResponse product, ApplicationStage stage, String reason) {
        List<ProductFeeResponse> matchingFees = Optional.ofNullable(product.getFees())
                .orElse(Collections.emptyList())
                .stream()
                .filter(fee -> Boolean.TRUE.equals(fee.getActive()))
                .filter(fee -> fee.getApplicationStage() == stage)
                .toList();

        BigDecimal total = BigDecimal.ZERO;

        if (loan.getFees() == null) {
            loan.setFees(new ArrayList<>());
        }

        if (loan.getTotalFees() == null) {
            loan.setTotalFees(BigDecimal.ZERO);
        }

        if (loan.getBalance() == null) {
            loan.setBalance(BigDecimal.ZERO);
        }

        for (ProductFeeResponse productFee : matchingFees) {
            BigDecimal amount = calculateFeeAmount(productFee, loan.getPrincipalAmount());

            LoanFee loanFee = LoanFee.builder()
                    .loan(loan)
                    .feeType(LoanFeeType.valueOf(productFee.getFeeType().name()))
                    .feeName(productFee.getFeeName())
                    .amount(amount)
                    .appliedAt(LocalDateTime.now())
                    .reason(reason)
                    .build();

            loanFeeRepository.save(loanFee);
            loan.getFees().add(loanFee);
            total = total.add(amount);
        }

        loan.setTotalFees(loan.getTotalFees().add(total));
        loan.setBalance(loan.getBalance().add(total));

        return total;
    }

    private BigDecimal calculateFeeAmount(ProductFeeResponse fee, BigDecimal principal) {
        if (fee.getCalculationType() == CalculationType.FIXED) {
            return fee.getAmount();
        }

        return principal
                .multiply(fee.getPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}