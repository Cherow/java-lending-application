package com.example.main.service.sender;

import com.example.main.model.enums.NotificationChannel;
import com.example.main.service.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(String recipient, String subject, String message) {
        log.info("Sending EMAIL to {} | subject={} | body={}", recipient, subject, message);

        if (!recipient.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }
}