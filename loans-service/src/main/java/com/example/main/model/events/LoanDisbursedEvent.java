package com.example.main.model.events;

import com.example.main.model.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanDisbursedEvent {
    private final Long customerId;
    private final Long loanId;
    private final String customerName;
    private final String phoneNumber;
    private final String loanNumber;
    private final NotificationEventType eventName;
}
