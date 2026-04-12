package com.example.main.repository;

import com.example.main.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByNationalId(String nationalId);
    Optional<Customer> findByCustomerNumber(String customerNumber);
}