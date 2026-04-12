package com.example.main.model.dto.response;

import com.example.main.model.enums.LoanFeeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanFeeResponse {
    private Long id;
    private LoanFeeType feeType;
    private String feeName;
    private BigDecimal amount;
    private LocalDateTime appliedAt;
    private String reason;
}
