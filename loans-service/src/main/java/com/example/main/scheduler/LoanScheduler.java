package com.example.main.scheduler;

import com.example.main.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanScheduler {

    private final LoanService loanService;

    @Scheduled(cron = "0 0 1 * * *")
    public void processOverdueLoans() throws SSLException {
        log.info("Starting overdue loan sweep job");
        loanService.markOverdueLoans();
        log.info("Completed overdue loan sweep job");
    }
}