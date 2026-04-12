package com.example.main.service;

import com.example.main.model.dto.request.CreateLoanRequest;
import com.example.main.model.dto.response.LoanResponse;
import com.example.main.model.dto.request.RepayLoanRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.util.List;
@Service
public interface LoanService {
    LoanResponse createLoan(CreateLoanRequest request) throws SSLException, JsonProcessingException;
    LoanResponse disburseLoan(Long loanId);
    LoanResponse repayLoan(Long loanId, RepayLoanRequest request);
    LoanResponse getLoanById(Long loanId);
    List<LoanResponse> getLoansByCustomer(Long customerId);
    void markOverdueLoans() throws SSLException;
    LoanResponse cancelLoan(Long loanId);
    LoanResponse writeOffLoan(Long loanId);
}