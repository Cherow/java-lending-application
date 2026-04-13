package com.example.main.eventListener;

import com.example.main.model.enums.NotificationEventType;
import com.example.main.service.eventHandler.NotificationEventHandler;
import com.example.main.service.eventHandler.NotificationEventHandlerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationEventHandlerFactory handlerFactory;
    private final ObjectMapper objectMapper =  new ObjectMapper();

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

            NotificationEventHandler handler = handlerFactory.getHandler(eventType);
            handler.handle(request);

        } catch (IllegalArgumentException ex) {
            log.error("Invalid NotificationEventType in AMQ message", ex);
        } catch (Exception ex) {
            log.error("Error processing AMQ message", ex);
        }
    }
}