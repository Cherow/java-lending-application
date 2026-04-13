package com.example.main.service.serviceImpl;

import com.example.main.mapper.NotificationMapper;
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
import com.example.main.service.NotificationService;
import com.example.main.service.TemplateRenderingService;
import com.example.main.service.sender.NotificationSender;
import com.example.main.service.sender.NotificationSenderFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;
    private final TemplateRenderingService templateRenderingService;
    private final NotificationSenderFactory senderFactory;

    @Override
    public NotificationTemplateResponse createTemplate(CreateNotificationTemplateRequest request) {


        if (templateRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Template code already exists");
        }

        NotificationTemplate template = NotificationTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .eventType(request.getEventType())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .bodyTemplate(request.getBodyTemplate())
                .active(request.getActive())
                .build();

        return NotificationMapper.toTemplateResponse(templateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLogResponse> getLogsByCustomer(Long customerId) {
        return logRepository.findByCustomerId(customerId).stream()
                .map(NotificationMapper::toLogResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLogResponse> getLogsByLoan(Long loanId) {
        return logRepository.findByLoanId(loanId).stream()
                .map(NotificationMapper::toLogResponse)
                .toList();
    }
    @Override
    public void send(NotificationEventType eventType,
                     NotificationChannel channel,
                     Long customerId,
                     Long loanId,
                     String recipient,
                     Map<String, String> variables) {
        log.info("Sending notification eventType={}, channel={}, recipient={}",
                eventType, channel, recipient);


        NotificationTemplate template = templateRepository
                .findByEventTypeAndChannelAndActiveTrue(eventType, channel)
                .orElseThrow(() -> new EntityNotFoundException("Active template not found"));

        String message = templateRenderingService.render(template.getBodyTemplate(), variables);
        String subject = template.getSubject() == null ? null : templateRenderingService.render(template.getSubject(), variables);

        NotificationLog log = NotificationLog.builder()
                .customerId(customerId)
                .loanId(loanId)
                .template(template)
                .eventType(eventType)
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        log = logRepository.save(log);

        try {
            simulateSend(channel, recipient, subject, message);
            log.setStatus(NotificationStatus.SENT);
            log.setSentAt(LocalDateTime.now());
        } catch (Exception ex) {
            log.setStatus(NotificationStatus.FAILED);
            log.setErrorMessage(ex.getMessage());
        }

        logRepository.save(log);
    }
    private void simulateSend(NotificationChannel channel,
                              String recipient,
                              String subject,
                              String message) {

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient is required");
        }

        NotificationSender sender = senderFactory.getSender(channel);
        sender.send(recipient, subject, message);
    }

    }


