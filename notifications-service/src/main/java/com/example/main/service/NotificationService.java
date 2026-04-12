package com.example.main.service;

import com.example.main.model.dto.CreateNotificationTemplateRequest;
import com.example.main.model.dto.NotificationLogResponse;
import com.example.main.model.dto.NotificationTemplateResponse;
import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request);
    List<NotificationLogResponse> getLogsByCustomer(Long customerId);
    List<NotificationLogResponse> getLogsByLoan(Long loanId);
    void send(NotificationEventType eventType,
              NotificationChannel channel,
              Long customerId,
              Long loanId,
              String recipient,
              Map<String, String> variables);
}
