package com.example.main.model.events;

import com.example.main.model.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoanWrittenOffEvent {
    private Long customerId;
    private Long loanId;
    private String customerName;
    private String phoneNumber;
    private String loanNumber;
    private BigDecimal outstandingBalance;
    private final NotificationEventType eventName;
}