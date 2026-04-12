package com.example.main.model.dto;

import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationTemplateResponse {
    private Long id;
    private String code;
    private String name;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String subject;
    private String bodyTemplate;
    private Boolean active;
}
