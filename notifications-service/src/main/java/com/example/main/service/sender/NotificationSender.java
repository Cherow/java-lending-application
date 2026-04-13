package com.example.main.service.sender;

import com.example.main.model.enums.NotificationChannel;

public interface NotificationSender {

    NotificationChannel getChannel();

    void send(String recipient, String subject, String message);
}