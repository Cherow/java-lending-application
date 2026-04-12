package com.example.main.repository;

import com.example.main.model.entity.LoanFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanFeeRepository extends JpaRepository<LoanFee, Long> {
    List<LoanFee> findByLoanId(Long loanId);
}