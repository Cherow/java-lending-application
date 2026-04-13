package com.example.main.repository;

import com.example.main.model.entity.Loan;
import com.example.main.model.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByLoanNumber(String loanNumber);
    List<Loan> findByCustomerId(Long customerId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDate date);
    List<Loan> findByStatusInAndDueDateBefore(
            List<LoanStatus> statuses,
            LocalDate date
    );
}