package com.example.main.service.sender;

import com.example.main.model.enums.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senderMap;

    public NotificationSenderFactory(List<NotificationSender> senders) {
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, s -> s));
    }

    public NotificationSender getSender(NotificationChannel channel) {
        NotificationSender sender = senderMap.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
        return sender;
    }
}