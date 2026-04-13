package com.example.main.service.eventHandler;

import com.example.main.model.enums.NotificationEventType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationEventHandlerFactory {

    private final Map<NotificationEventType, NotificationEventHandler> handlerMap;

    public NotificationEventHandlerFactory(List<NotificationEventHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(NotificationEventHandler::getEventType, h -> h));
    }

    public NotificationEventHandler getHandler(NotificationEventType eventType) {
        NotificationEventHandler handler = handlerMap.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("No handler found for event type: " + eventType);
        }
        return handler;
    }
}