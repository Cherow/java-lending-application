package com.example.main.service.eventHandler;

import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import com.example.main.model.events.LoanCancelledEvent;
import com.example.main.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanCancelledEventHandler implements NotificationEventHandler {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NotificationEventType getEventType() {
        return NotificationEventType.LOAN_CANCELLED;
    }

    @Override
    public void handle(String request) {
        try {
            LoanCancelledEvent event = objectMapper.readValue(request, LoanCancelledEvent.class);

            log.info("Received LoanCancelledEvent for loanId={}", event.getLoanId());

            notificationService.send(
                    event.getEventName(),
                    NotificationChannel.SMS,
                    event.getCustomerId(),
                    event.getLoanId(),
                    event.getPhoneNumber(),
                    Map.of(
                            "customerName", event.getCustomerName(),
                            "loanNumber", event.getLoanNumber()
                    )
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to handle LOAN_CANCELLED event", ex);
        }
    }
}