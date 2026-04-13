package com.example.main.service.eventHandler;

import com.example.main.model.enums.NotificationEventType;

public interface NotificationEventHandler {

    NotificationEventType getEventType();

    void handle(String request);
}