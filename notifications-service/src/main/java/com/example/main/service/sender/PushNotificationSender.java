package com.example.main.service.sender;

import com.example.main.model.enums.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(String recipient, String subject, String message) {
        log.info("Sending PUSH to device {}: {}", recipient, message);

        if (recipient.length() < 5) {
            throw new IllegalArgumentException("Invalid device ID");
        }
    }
}