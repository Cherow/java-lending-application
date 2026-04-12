package com.example.main.eventListener;

import com.example.main.model.events.LoanCancelledEvent;
import com.example.main.model.events.LoanCreatedEvent;
import com.example.main.model.events.LoanDisbursedEvent;
import com.example.main.model.events.LoanOverdueEvent;
import com.example.main.model.events.LoanRepaidEvent;
import com.example.main.model.events.LoanWrittenOffEvent;
import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import com.example.main.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.main.model.enums.NotificationEventType;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${amq.queue-name}")
    private String destinationQueue;

    @JmsListener(destination = "${amq.queue-name}")
    public void messageReceiver(Message message) {
        try {
            log.info("AMQ message received from queue {}", destinationQueue);

            if (!(message instanceof TextMessage textMessage)) {
                log.error("AMQ error: message is not a TextMessage");
                return;
            }

            String request = textMessage.getText();
            log.info("AMQ request: {}", request);

            JsonNode root = objectMapper.readTree(request);
            String eventName = root.path("eventName").asText();

            if (eventName == null || eventName.isBlank()) {
                log.error("AMQ error: eventName is missing in payload");
                return;
            }

            NotificationEventType eventType = NotificationEventType.valueOf(eventName);

            switch (eventType) {
                case LOAN_CREATED -> {
                    LoanCreatedEvent event = objectMapper.readValue(request, LoanCreatedEvent.class);
                    handleLoanCreated(event);
                }
                case LOAN_DISBURSED -> {
                    LoanDisbursedEvent event = objectMapper.readValue(request, LoanDisbursedEvent.class);
                    handleLoanDisbursed(event);
                }
                case LOAN_REPAID -> {
                    LoanRepaidEvent event = objectMapper.readValue(request, LoanRepaidEvent.class);
                    handleLoanRepaid(event);
                }
                case LOAN_OVERDUE -> {
                    LoanOverdueEvent event = objectMapper.readValue(request, LoanOverdueEvent.class);
                    handleLoanOverdue(event);
                }
                case LOAN_CANCELLED -> {
                    LoanCancelledEvent event = objectMapper.readValue(request, LoanCancelledEvent.class);
                    handleLoanCancelled(event);
                }
                case LOAN_WRITTEN_OFF -> {
                    LoanWrittenOffEvent event = objectMapper.readValue(request, LoanWrittenOffEvent.class);
                    handleLoanWrittenOff(event);
                }
                default -> log.warn("Unhandled notification event type {}", eventType);
            }

        } catch (IllegalArgumentException ex) {
            log.error("Invalid NotificationEventType in AMQ message", ex);
        } catch (Exception ex) {
            log.error("Error processing AMQ message", ex);
        }
    }

    public void handleLoanCreated(LoanCreatedEvent event) {
        log.info("Received LoanCreatedEvent for loanId={}", event.getLoanId());
        notificationService.send(
                event.getEventName(),
                NotificationChannel.SMS,
                event.getCustomerId(),
                event.getLoanId(),
                event.getPhoneNumber(),
                Map.of(
                        "customerName", event.getCustomerName(),
                        "loanNumber", event.getLoanNumber(),
                        "amount", event.getAmount().toPlainString()
                )
        );
    }

    public void handleLoanDisbursed(LoanDisbursedEvent event) {
        log.info("Received LoanDisbursedEvent for loanId={}", event.getLoanId());
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
    }

    public void handleLoanRepaid(LoanRepaidEvent event) {
        log.info("Received LoanRepaidEvent for loanId={}", event.getLoanId());
        notificationService.send(
                event.getEventName(),
                NotificationChannel.SMS,
                event.getCustomerId(),
                event.getLoanId(),
                event.getPhoneNumber(),
                Map.of(
                        "customerName", event.getCustomerName(),
                        "loanNumber", event.getLoanNumber(),
                        "amountPaid", event.getAmountPaid().toPlainString()
                )
        );
    }

    public void handleLoanOverdue(LoanOverdueEvent event) {
        log.info("Received LoanOverdueEvent for loanId={}", event.getLoanId());
        notificationService.send(
                event.getEventName(),
                NotificationChannel.SMS,
                event.getCustomerId(),
                event.getLoanId(),
                event.getPhoneNumber(),
                Map.of(
                        "customerName", event.getCustomerName(),
                        "loanNumber", event.getLoanNumber(),
                        "outstandingBalance", event.getOutstandingBalance().toPlainString()
                )
        );
    }

    public void handleLoanCancelled(LoanCancelledEvent event) {
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
    }

    public void handleLoanWrittenOff(LoanWrittenOffEvent event) {
        log.info("Received LoanWrittenOffEvent for loanId={}", event.getLoanId());
        notificationService.send(
                event.getEventName(),
                NotificationChannel.SMS,
                event.getCustomerId(),
                event.getLoanId(),
                event.getPhoneNumber(),
                Map.of(
                        "customerName", event.getCustomerName(),
                        "loanNumber", event.getLoanNumber(),
                        "outstandingBalance", event.getOutstandingBalance().toPlainString()
                )
        );
    }
}