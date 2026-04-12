package com.example.main.mapper;

import com.example.main.model.dto.response.LoanFeeResponse;
import com.example.main.model.dto.response.LoanRepaymentResponse;
import com.example.main.model.dto.response.LoanResponse;
import com.example.main.model.entity.Loan;
import com.example.main.model.entity.LoanFee;
import com.example.main.model.entity.LoanRepayment;

import java.util.stream.Collectors;

public class LoanMapper {

    public static LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanNumber(loan.getLoanNumber())
                .customerId(loan.getCustomerId())
                .productId(loan.getProductId())
                .principalAmount(loan.getPrincipalAmount())
                .disbursedAmount(loan.getDisbursedAmount())
                .totalFees(loan.getTotalFees())
                .totalPaid(loan.getTotalPaid())
                .balance(loan.getBalance())
                .tenure(loan.getTenure())
                .disbursementDate(loan.getDisbursementDate())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .fees(loan.getFees().stream().map(LoanMapper::toFeeResponse).collect(Collectors.toList()))
                .repayments(loan.getRepayments().stream().map(LoanMapper::toRepaymentResponse).collect(Collectors.toList()))
                .build();
    }

    public static LoanFeeResponse toFeeResponse(LoanFee fee) {
        return LoanFeeResponse.builder()
                .id(fee.getId())
                .feeType(fee.getFeeType())
                .feeName(fee.getFeeName())
                .amount(fee.getAmount())
                .appliedAt(fee.getAppliedAt())
                .reason(fee.getReason())
                .build();
    }

    public static LoanRepaymentResponse toRepaymentResponse(LoanRepayment repayment) {
        return LoanRepaymentResponse.builder()
                .id(repayment.getId())
                .amountPaid(repayment.getAmountPaid())
                .paymentReference(repayment.getPaymentReference())
                .paymentChannel(repayment.getPaymentChannel())
                .status(repayment.getStatus())
                .paymentDate(repayment.getPaymentDate())
                .build();
    }
}