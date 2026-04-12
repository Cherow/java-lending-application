package com.example.main.model.events;

import com.example.main.model.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaidEvent {
    private  Long customerId;
    private  Long loanId;
    private  String customerName;
    private  String phoneNumber;
    private  String loanNumber;
    private  BigDecimal amountPaid;
    private  NotificationEventType eventName;
}
