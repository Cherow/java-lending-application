package com.example.main.notificationModule;

import com.example.main.model.dto.CreateNotificationTemplateRequest;
import com.example.main.model.dto.NotificationLogResponse;
import com.example.main.model.dto.NotificationTemplateResponse;
import com.example.main.model.entity.NotificationLog;
import com.example.main.model.entity.NotificationTemplate;
import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import com.example.main.model.enums.NotificationStatus;
import com.example.main.repository.NotificationLogRepository;
import com.example.main.repository.NotificationTemplateRepository;
import com.example.main.service.TemplateRenderingService;
import com.example.main.service.serviceImpl.NotificationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationLogRepository logRepository;

    @Mock
    private TemplateRenderingService templateRenderingService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    // ===================== CREATE TEMPLATE =====================

    @Test
    void createTemplate_shouldCreateSuccessfully() {
        CreateNotificationTemplateRequest request = new CreateNotificationTemplateRequest();
        request.setCode("WELCOME");
        request.setName("Welcome Template");
        request.setEventType(NotificationEventType.LOAN_CREATED);
        request.setChannel(NotificationChannel.SMS);
        request.setSubject("Hello");
        request.setBodyTemplate("Hi {{name}}");
        request.setActive(true);

        NotificationTemplate template = NotificationTemplate.builder()
                .id(1L)
                .code("WELCOME")
                .name("Welcome Template")
                .build();

        when(templateRepository.existsByCode("WELCOME")).thenReturn(false);
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);

        NotificationTemplateResponse response = notificationService.createTemplate(request);

        assertNotNull(response);
        assertEquals("WELCOME", response.getCode());

        verify(templateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    void createTemplate_shouldThrowWhenCodeExists() {
        CreateNotificationTemplateRequest request = new CreateNotificationTemplateRequest();
        request.setCode("WELCOME");

        when(templateRepository.existsByCode("WELCOME")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.createTemplate(request)
        );

        assertEquals("Template code already exists", ex.getMessage());
    }

    // ===================== GET LOGS =====================

    @Test
    void getLogsByCustomer_shouldReturnLogs() {
        NotificationLog log = NotificationLog.builder()
                .id(1L)
                .customerId(1L)
                .message("Test message")
                .status(NotificationStatus.SENT)
                .build();

        when(logRepository.findByCustomerId(1L)).thenReturn(List.of(log));

        List<NotificationLogResponse> responses = notificationService.getLogsByCustomer(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getLogsByLoan_shouldReturnLogs() {
        NotificationLog log = NotificationLog.builder()
                .id(1L)
                .loanId(10L)
                .message("Loan message")
                .status(NotificationStatus.SENT)
                .build();

        when(logRepository.findByLoanId(10L)).thenReturn(List.of(log));

        List<NotificationLogResponse> responses = notificationService.getLogsByLoan(10L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    // ===================== SEND SUCCESS =====================

    @Test
    void send_shouldSendSuccessfully() {
        NotificationTemplate template = NotificationTemplate.builder()
                .id(1L)
                .bodyTemplate("Hello {{name}}")
                .subject("Hi {{name}}")
                .build();

        NotificationLog savedLog = NotificationLog.builder()
                .id(1L)
                .status(NotificationStatus.PENDING)
                .build();

        when(templateRepository.findByEventTypeAndChannelAndActiveTrue(
                NotificationEventType.LOAN_CREATED,
                NotificationChannel.SMS))
                .thenReturn(Optional.of(template));

        when(templateRenderingService.render(anyString(), anyMap()))
                .thenReturn("Rendered");

        when(logRepository.save(any(NotificationLog.class)))
                .thenReturn(savedLog);

        notificationService.send(
                NotificationEventType.LOAN_CREATED,
                NotificationChannel.SMS,
                1L,
                10L,
                "0712345678",
                Map.of("name", "Mercy")
        );

        verify(logRepository, times(2)).save(any(NotificationLog.class));
    }

    // ===================== SEND FAILURE =====================

    @Test
    void send_shouldFailWhenRecipientIsNull() {
        NotificationTemplate template = NotificationTemplate.builder()
                .id(1L)
                .bodyTemplate("Hello")
                .build();

        NotificationLog savedLog = NotificationLog.builder()
                .id(1L)
                .status(NotificationStatus.PENDING)
                .build();

        when(templateRepository.findByEventTypeAndChannelAndActiveTrue(
                NotificationEventType.LOAN_CREATED,
                NotificationChannel.SMS))
                .thenReturn(Optional.of(template));

        when(templateRenderingService.render(anyString(), anyMap()))
                .thenReturn("Rendered");

        when(logRepository.save(any(NotificationLog.class)))
                .thenReturn(savedLog);

        notificationService.send(
                NotificationEventType.LOAN_CREATED,
                NotificationChannel.SMS,
                1L,
                10L,
                null, // ❌ triggers failure
                Map.of()
        );

        verify(logRepository, times(2)).save(any(NotificationLog.class));
    }

    // ===================== TEMPLATE NOT FOUND =====================

    @Test
    void send_shouldThrowWhenTemplateNotFound() {
        when(templateRepository.findByEventTypeAndChannelAndActiveTrue(
                NotificationEventType.LOAN_CREATED,
                NotificationChannel.SMS))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> notificationService.send(
                        NotificationEventType.LOAN_CREATED,
                        NotificationChannel.SMS,
                        1L,
                        10L,
                        "0712345678",
                        Map.of()
                )
        );

        assertEquals("Active template not found", ex.getMessage());
    }
}