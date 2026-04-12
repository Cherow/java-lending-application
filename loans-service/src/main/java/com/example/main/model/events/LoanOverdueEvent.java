package com.example.main.model.events;

import com.example.main.model.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LoanOverdueEvent {
    private final Long customerId;
    private final Long loanId;
    private final String customerName;
    private final String phoneNumber;
    private final String loanNumber;
    private final BigDecimal outstandingBalance;
    private final NotificationEventType eventName;
}
