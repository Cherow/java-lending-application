package com.example.main.publisher;

import com.example.main.common.Utils;
import com.example.main.model.events.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanEventPublisher {

    private final JmsTemplate jmsTemplate;

    @Value("${amq.queue-name}")
    private String destinationQueue;

    public void publishLoanCreated(LoanCreatedEvent event) {
        send(event);
    }

    public void publishLoanDisbursed(LoanDisbursedEvent event) {
        send(event);
    }

    public void publishLoanRepaid(LoanRepaidEvent event) {
        send(event);
    }

    public void publishLoanOverdue(LoanOverdueEvent event) {
        send(event);
    }

    public void publishLoanCancelled(LoanCancelledEvent event) {
        send(event);
    }

    public void publishLoanWrittenOff(LoanWrittenOffEvent event) {
        send(event);
    }

    private void send(Object payload) {
        try {
            String request = Utils.setJsonString(payload);

            log.info("Sending event to queue {}", destinationQueue);

            jmsTemplate.send(destinationQueue, session -> {
                TextMessage message = session.createTextMessage();
                message.setText(request);
                return message;
            });

            log.info("Event successfully written to queue");
        } catch (Exception ex) {
            log.error("Failed to send event to queue", ex);
            throw new RuntimeException("Failed to send event to queue", ex);
        }
    }
}