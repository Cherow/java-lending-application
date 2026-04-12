package com.example.main.model.dto;

import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNotificationTemplateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private NotificationEventType eventType;

    @NotNull
    private NotificationChannel channel;

    private String subject;

    @NotBlank
    private String bodyTemplate;

    @NotNull
    private Boolean active;
}