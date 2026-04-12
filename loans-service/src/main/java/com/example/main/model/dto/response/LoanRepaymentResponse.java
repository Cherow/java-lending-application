package com.example.main.model.dto.response;

import com.example.main.model.enums.RepaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanRepaymentResponse {
    private Long id;
    private BigDecimal amountPaid;
    private String paymentReference;
    private String paymentChannel;
    private RepaymentStatus status;
    private LocalDateTime paymentDate;
}
