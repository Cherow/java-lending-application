package com.example.main.service.serviceImpl;

import com.example.main.factory.LoanDomainFactory;
import com.example.main.gateway.CustomerGateway;
import com.example.main.gateway.ProductGateway;
import com.example.main.mapper.LoanNotificationEventMapper;
import com.example.main.mapper.LoanMapper;
import com.example.main.model.dto.request.CreateLoanRequest;
import com.example.main.model.dto.request.RepayLoanRequest;
import com.example.main.model.dto.response.CustomerResponse;
import com.example.main.model.dto.response.LoanResponse;
import com.example.main.model.dto.response.ProductResponse;
import com.example.main.model.entity.Loan;
import com.example.main.model.entity.LoanRepayment;
import com.example.main.model.enums.LoanStatus;
import com.example.main.publisher.LoanEventPublisher;
import com.example.main.repository.LoanRepaymentRepository;
import com.example.main.repository.LoanRepository;
import com.example.main.service.FeeCalculationService;
import com.example.main.service.LoanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final FeeCalculationService feeCalculationService;
    private final CustomerGateway customerGateway;
    private final ProductGateway productGateway;
    private final LoanEventPublisher loanEventPublisher;
    private final LoanNotificationEventMapper loanNotificationEventMapper;
    private final LoanDomainFactory loanDomainFactory;

    @Override
    public LoanResponse createLoan(CreateLoanRequest request) {
        ProductResponse product = productGateway.getProductById(request.getProductId());
        CustomerResponse customer = customerGateway.getCustomerById(request.getCustomerId());

        validateRequestedTenure(request.getTenure(), product);
        customerGateway.validateCustomerForLoan(request.getCustomerId(), request.getPrincipalAmount());

        Loan savedLoan = loanRepository.save(loanDomainFactory.buildLoan(request, product));

        feeCalculationService.applyOriginationFees(savedLoan, product);
        Loan finalLoan = loanRepository.save(savedLoan);

        loanEventPublisher.publishLoanCreated(
                loanNotificationEventMapper.toLoanCreatedEvent(finalLoan, customer)
        );

        return LoanMapper.toResponse(finalLoan);
    }

    @Override
    public LoanResponse disburseLoan(Long loanId) {
        Loan loan = getLoanEntity(loanId);
        validateLoanCanBeDisbursed(loan);

        customerGateway.consumeLimit(loan.getCustomerId(), loan.getPrincipalAmount());

        loan.setDisbursedAmount(loan.getPrincipalAmount());
        loan.setDisbursementDate(LocalDate.now());
        loan.setStatus(LoanStatus.DISBURSED);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = customerGateway.getCustomerById(savedLoan.getCustomerId());

        loanEventPublisher.publishLoanDisbursed(
                loanNotificationEventMapper.toLoanDisbursedEvent(savedLoan, customer)
        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponse repayLoan(Long loanId, RepayLoanRequest request) {
        Loan loan = getLoanEntity(loanId);
        validateRepaymentAllowed(loan);

        LoanRepayment repayment = loanDomainFactory.buildLoanRepayment(loan, request);
        loanRepaymentRepository.save(repayment);
        loan.getRepayments().add(repayment);

        updateLoanAfterRepayment(loan, request.getAmount());
        customerGateway.releaseLimit(loan.getCustomerId(), request.getAmount());

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = customerGateway.getCustomerById(savedLoan.getCustomerId());

        loanEventPublisher.publishLoanRepaid(
                loanNotificationEventMapper.toLoanRepaidEvent(savedLoan, customer, request)
        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponse cancelLoan(Long loanId) {
        Loan loan = getLoanEntity(loanId);
        validateLoanCanBeCancelled(loan);

        loan.setStatus(LoanStatus.CANCELLED);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = customerGateway.getCustomerById(savedLoan.getCustomerId());

        loanEventPublisher.publishLoanCancelled(
                loanNotificationEventMapper.toLoanCancelledEvent(savedLoan, customer)
        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponse writeOffLoan(Long loanId) {
        Loan loan = getLoanEntity(loanId);
        validateLoanCanBeWrittenOff(loan);

        loan.setStatus(LoanStatus.WRITTEN_OFF);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = customerGateway.getCustomerById(savedLoan.getCustomerId());

        loanEventPublisher.publishLoanWrittenOff(
                loanNotificationEventMapper.toLoanWrittenOffEvent(savedLoan, customer)
        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        return LoanMapper.toResponse(getLoanEntity(loanId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomer(Long customerId) {
        customerGateway.getCustomerById(customerId);

        return loanRepository.findByCustomerId(customerId)
                .stream()
                .map(LoanMapper::toResponse)
                .toList();
    }

    @Override
    public void markOverdueLoans() {
        List<Loan> overdueCandidates = loanRepository
                .findByStatusInAndDueDateBefore(
                        List.of(LoanStatus.DISBURSED, LoanStatus.OPEN),
                        LocalDate.now()
                );

        overdueCandidates.stream()
                .filter(loan -> loan.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .forEach(this::processOverdueLoan);
    }

    private void processOverdueLoan(Loan loan) {
        loan.setStatus(LoanStatus.OVERDUE);

        ProductResponse product = productGateway.getProductById(loan.getProductId());
        feeCalculationService.applyLateFees(loan, product);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = customerGateway.getCustomerById(savedLoan.getCustomerId());

        loanEventPublisher.publishLoanOverdue(
                loanNotificationEventMapper.toLoanOverdueEvent(savedLoan, customer)
        );
    }

    private Loan getLoanEntity(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with id: " + loanId));
    }

    private void validateRequestedTenure(Integer tenure, ProductResponse product) {
        if (tenure < product.getMinTenure() || tenure > product.getMaxTenure()) {
            throw new IllegalArgumentException("Requested tenure is outside product range");
        }
    }

    private void validateLoanCanBeDisbursed(Loan loan) {
        if (loan.getStatus() != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be disbursed");
        }
    }

    private void validateLoanCanBeCancelled(Loan loan) {
        if (loan.getStatus() != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be cancelled");
        }
    }

    private void validateLoanCanBeWrittenOff(Loan loan) {
        if (loan.getStatus() != LoanStatus.OPEN && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Only OPEN or OVERDUE loans can be written off");
        }

        if (loan.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Only loans with outstanding balance can be written off");
        }
    }

    private void validateRepaymentAllowed(Loan loan) {
        if (loan.getStatus() == LoanStatus.CLOSED
                || loan.getStatus() == LoanStatus.CANCELLED
                || loan.getStatus() == LoanStatus.WRITTEN_OFF) {
            throw new IllegalStateException("Repayment is not allowed for this loan status");
        }
    }

    private void updateLoanAfterRepayment(Loan loan, BigDecimal amount) {
        loan.setTotalPaid(loan.getTotalPaid().add(amount));
        loan.setBalance(loan.getBalance().subtract(amount));

        if (loan.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setBalance(BigDecimal.ZERO);
            loan.setStatus(LoanStatus.CLOSED);
        } else if (loan.getStatus() == LoanStatus.DISBURSED) {
            loan.setStatus(LoanStatus.OPEN);
        }
    }
}