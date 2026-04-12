package com.example.main.repository;

import com.example.main.model.entity.NotificationTemplate;
import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByEventTypeAndChannelAndActiveTrue(NotificationEventType eventType, NotificationChannel channel);
    boolean existsByCode(String code);
}
