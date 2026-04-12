package com.example.main.model.events;

import com.example.main.model.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoanCancelledEvent {
    private Long customerId;
    private Long loanId;
    private String customerName;
    private String phoneNumber;
    private String loanNumber;
    private final NotificationEventType eventName;
}