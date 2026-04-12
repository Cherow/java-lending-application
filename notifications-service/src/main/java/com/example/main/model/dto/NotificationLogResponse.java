package com.example.main.model.dto;

import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import com.example.main.model.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationLogResponse {
    private Long id;
    private Long customerId;
    private Long loanId;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String message;
    private NotificationStatus status;
    private String errorMessage;
    private LocalDateTime sentAt;
}
