package com.example.main.controller;

import com.example.main.model.dto.request.CreateLoanRequest;
import com.example.main.model.dto.response.LoanResponse;
import com.example.main.model.dto.request.RepayLoanRequest;
import com.example.main.service.LoanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.SSLException;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) throws SSLException, JsonProcessingException {
        return new ResponseEntity<>(loanService.createLoan(request), HttpStatus.CREATED);
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<LoanResponse> disburseLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.disburseLoan(loanId));
    }

    @PostMapping("/{loanId}/repayments")
    public ResponseEntity<LoanResponse> repayLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody RepayLoanRequest request) {
        return ResponseEntity.ok(loanService.repayLoan(loanId, request));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanResponse>> getLoansByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanService.getLoansByCustomer(customerId));
    }

    @PostMapping("/{loanId}/cancel")
    public ResponseEntity<LoanResponse> cancelLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.cancelLoan(loanId));
    }

    @PostMapping("/{loanId}/write-off")
    public ResponseEntity<LoanResponse> writeOffLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.writeOffLoan(loanId));
    }

//    @PostMapping("/test/overdue-sweep")
//    public ResponseEntity<String> runOverdueSweep() {
//        loanService.markOverdueLoans();
//        return ResponseEntity.ok("Sweep executed successfully");
//    }
}