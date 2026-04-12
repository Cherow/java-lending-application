package com.example.main.model.events;

import com.example.main.model.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreatedEvent {
    private  Long customerId;
    private  Long loanId;
    private  String customerName;
    private  String phoneNumber;
    private  BigDecimal amount;
    private  String loanNumber;
    private  NotificationEventType eventName;
}
