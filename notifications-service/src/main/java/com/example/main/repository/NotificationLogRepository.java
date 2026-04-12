package com.example.main.repository;

import com.example.main.model.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByCustomerId(Long customerId);
    List<NotificationLog> findByLoanId(Long loanId);
}
