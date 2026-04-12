package com.example.main.controller;
import com.example.main.model.dto.CreateNotificationTemplateRequest;
import com.example.main.model.dto.NotificationLogResponse;
import com.example.main.model.dto.NotificationTemplateResponse;
import com.example.main.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(@Valid @RequestBody CreateNotificationTemplateRequest request) {
        return new ResponseEntity<>(notificationService.createTemplate(request), HttpStatus.CREATED);
    }

    @GetMapping("/customers/{customerId}/logs")
    public ResponseEntity<List<NotificationLogResponse>> getLogsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getLogsByCustomer(customerId));
    }
    @GetMapping("/loans/{loanId}/logs")
    public ResponseEntity<List<NotificationLogResponse>> getLogsByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(notificationService.getLogsByLoan(loanId));
    }
}
