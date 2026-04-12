package com.example.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.main.model.enums.NotificationChannel;
import com.example.main.model.enums.NotificationEventType;
import com.example.main.model.enums.NotificationStatus;



@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private Long loanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 200)
    private String recipient;

    @Column(length = 200)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(length = 500)
    private String errorMessage;

    private LocalDateTime sentAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}