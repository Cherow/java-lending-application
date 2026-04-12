package com.example.main.mapper;

import com.example.main.model.dto.NotificationLogResponse;
import com.example.main.model.dto.NotificationTemplateResponse;
import com.example.main.model.entity.NotificationLog;
import com.example.main.model.entity.NotificationTemplate;

public class NotificationMapper {

    public static NotificationTemplateResponse toTemplateResponse(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .eventType(template.getEventType())
                .channel(template.getChannel())
                .subject(template.getSubject())
                .bodyTemplate(template.getBodyTemplate())
                .active(template.getActive())
                .build();
    }

    public static NotificationLogResponse toLogResponse(NotificationLog log) {
        return NotificationLogResponse.builder()
                .id(log.getId())
                .customerId(log.getCustomerId())
                .loanId(log.getLoanId())
                .eventType(log.getEventType())
                .channel(log.getChannel())
                .recipient(log.getRecipient())
                .subject(log.getSubject())
                .message(log.getMessage())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .sentAt(log.getSentAt())
                .build();
    }
}
