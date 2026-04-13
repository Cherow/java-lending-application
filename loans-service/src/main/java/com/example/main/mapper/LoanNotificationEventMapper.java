package com.example.main.mapper;

import com.example.main.model.dto.request.RepayLoanRequest;
import com.example.main.model.dto.response.CustomerResponse;
import com.example.main.model.entity.Loan;
import com.example.main.model.enums.NotificationEventType;
import com.example.main.model.events.LoanCancelledEvent;
import com.example.main.model.events.LoanCreatedEvent;
import com.example.main.model.events.LoanDisbursedEvent;
import com.example.main.model.events.LoanOverdueEvent;
import com.example.main.model.events.LoanRepaidEvent;
import com.example.main.model.events.LoanWrittenOffEvent;
import org.springframework.stereotype.Component;

@Component
public class LoanNotificationEventMapper {

    public LoanCreatedEvent toLoanCreatedEvent(Loan loan, CustomerResponse customer) {
        return new LoanCreatedEvent(
                loan.getCustomerId(),
                loan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                loan.getPrincipalAmount(),
                loan.getLoanNumber(),
                NotificationEventType.LOAN_CREATED
        );
    }

    public LoanDisbursedEvent toLoanDisbursedEvent(Loan loan, CustomerResponse customer) {
        return new LoanDisbursedEvent(
                loan.getCustomerId(),
                loan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                loan.getLoanNumber(),
                NotificationEventType.LOAN_DISBURSED
        );
    }

    public LoanRepaidEvent toLoanRepaidEvent(Loan loan, CustomerResponse customer, RepayLoanRequest request) {
        return new LoanRepaidEvent(
                loan.getCustomerId(),
                loan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                loan.getLoanNumber(),
                request.getAmount(),
                NotificationEventType.LOAN_REPAID
        );
    }

    public LoanCancelledEvent toLoanCancelledEvent(Loan loan, CustomerResponse customer) {
        return new LoanCancelledEvent(
                loan.getCustomerId(),
                loan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                loan.getLoanNumber(),
                NotificationEventType.LOAN_CANCELLED
        );
    }

    public LoanWrittenOffEvent toLoanWrittenOffEvent(Loan loan, CustomerResponse customer) {
        return new LoanWrittenOffEvent(
                loan.getCustomerId(),
                loan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                loan.getLoanNumber(),
                loan.getBalance(),
                NotificationEventType.LOAN_WRITTEN_OFF
        );
    }

    public LoanOverdueEvent toLoanOverdueEvent(Loan loan, CustomerResponse customer) {
        return new LoanOverdueEvent(
                loan.getCustomerId(),
                loan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                loan.getLoanNumber(),
                loan.getBalance(),
                NotificationEventType.LOAN_OVERDUE
        );
    }

    private String buildCustomerName(CustomerResponse customer) {
        return customer.getFirstName() + " " + customer.getLastName();
    }
}