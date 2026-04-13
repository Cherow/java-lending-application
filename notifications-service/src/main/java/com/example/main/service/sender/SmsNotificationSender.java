package com.example.main.service.sender;

import com.example.main.model.enums.NotificationChannel;
import com.example.main.service.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(String recipient, String subject, String message) {
        log.info("Sending SMS to {}: {}", recipient, message);

        if (recipient.length() < 10) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }
}