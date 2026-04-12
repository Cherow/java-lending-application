package com.example.main.repository;

import com.example.main.model.entity.CustomerLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerLimitRepository extends JpaRepository<CustomerLimit, Long> {
    Optional<CustomerLimit> findByCustomerId(Long customerId);
}