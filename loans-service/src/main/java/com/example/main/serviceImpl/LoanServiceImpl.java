package com.example.main.serviceImpl;

import com.example.main.common.CustomerClient;
import com.example.main.common.ProductClient;
import com.example.main.common.Utils;
import com.example.main.mapper.LoanMapper;
import com.example.main.model.dto.request.CreateLoanRequest;
import com.example.main.model.dto.response.LoanResponse;
import com.example.main.model.dto.request.RepayLoanRequest;
import com.example.main.model.entity.Loan;
import com.example.main.model.entity.LoanRepayment;
import com.example.main.model.enums.LoanStatus;
import com.example.main.model.enums.RepaymentStatus;
import com.example.main.model.events.LoanCreatedEvent;
import com.example.main.model.events.LoanDisbursedEvent;
import com.example.main.model.events.LoanOverdueEvent;
import com.example.main.model.events.LoanRepaidEvent;
import com.example.main.repository.LoanRepaymentRepository;
import com.example.main.repository.LoanRepository;
import com.example.main.service.FeeCalculationService;
import com.example.main.service.LoanService;
import com.example.main.model.dto.response.CustomerResponse;
import com.example.main.model.dto.response.ProductResponse;
import com.example.main.model.enums.NotificationEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.jms.TextMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.main.model.events.LoanCancelledEvent;
import com.example.main.model.events.LoanWrittenOffEvent;

import javax.net.ssl.SSLException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {
    @Value("${amq.queue-name}")
    private String destinationQueue ;

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final FeeCalculationService feeCalculationService;
    private final CustomerClient customerService;
    private final ProductClient productService;
    private final ApplicationEventPublisher eventPublisher;



    private final JmsTemplate jmsTemplate;

    @Override
    public LoanResponse createLoan(CreateLoanRequest request) throws SSLException, JsonProcessingException {

        String stringResponse= productService.getProductById(request.getProductId());
        log.info("Creating loan by id {}", stringResponse);
        ProductResponse product = (ProductResponse) Utils.setJsonStringToObject(stringResponse,ProductResponse.class);

       log.info("Product {}", product.getId());
       log.info("Creating loan for product {}", Utils.setJsonString(product));

        CustomerResponse customer = getCustomerEntity(request.getCustomerId());

        validateRequestedTenure(request.getTenure(), product);

        // Validate customer eligibility and available limit
        // But do NOT consume yet. We consume on disbursement.
        customerService.validateCustomerForLoan(request.getCustomerId(), request.getPrincipalAmount());

        LocalDate dueDate = resolveDueDate(LocalDate.now(), request.getTenure(), product);

        Loan loan = Loan.builder()
                .loanNumber(generateLoanNumber())
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .principalAmount(request.getPrincipalAmount())
                .disbursedAmount(BigDecimal.ZERO)
                .totalFees(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .balance(request.getPrincipalAmount())
                .tenure(request.getTenure())
                .disbursementDate(null)
                .dueDate(dueDate)
                .status(LoanStatus.CREATED)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        log.info("Saved loan {}", savedLoan);

        feeCalculationService.applyOriginationFees(savedLoan, product);
        Loan finalLoan = loanRepository.save(savedLoan);

        log.info("Publishing LoanCreatedEvent for loanId={}", finalLoan.getId());
        LoanCreatedEvent loanCreatedEvent = new LoanCreatedEvent(finalLoan.getCustomerId(),
                finalLoan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                finalLoan.getPrincipalAmount(),
                finalLoan.getLoanNumber(),
                NotificationEventType.LOAN_CREATED);
        sendToQueue(Utils.setJsonString(loanCreatedEvent));
//        eventPublisher.publishEvent(
//                new LoanCreatedEvent(
//                        finalLoan.getCustomerId(),
//                        finalLoan.getId(),
//                        buildCustomerName(customer),
//                        customer.getPhoneNumber(),
//                        finalLoan.getPrincipalAmount(),
//                        finalLoan.getLoanNumber()
//                )
//        );

        return LoanMapper.toResponse(finalLoan);
    }

    @Override
    public LoanResponse disburseLoan(Long loanId) {
        Loan loan = getLoanEntity(loanId);

        if (loan.getStatus() != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be disbursed");
        }

        // Consume customer limit here because funds are now being committed
        customerService.consumeLimit(loan.getCustomerId(), loan.getPrincipalAmount());

        loan.setDisbursedAmount(loan.getPrincipalAmount());
        loan.setDisbursementDate(LocalDate.now());
        loan.setStatus(LoanStatus.DISBURSED);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = getCustomerEntity(savedLoan.getCustomerId());

        log.info("Publishing disburseLoan for loanId={}", savedLoan.getId());
        LoanDisbursedEvent loanDisbursedEvent = new LoanDisbursedEvent(savedLoan.getCustomerId(),
                savedLoan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                savedLoan.getLoanNumber(),
                NotificationEventType.LOAN_DISBURSED);
        sendToQueue(Utils.setJsonString(loanDisbursedEvent));
//        eventPublisher.publishEvent(
//                new LoanDisbursedEvent(
//                        savedLoan.getCustomerId(),
//                        savedLoan.getId(),
//                        buildCustomerName(customer),
//                        customer.getPhoneNumber(),
//                        savedLoan.getLoanNumber()
//                )
//        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponse repayLoan(Long loanId, RepayLoanRequest request) {
        Loan loan = getLoanEntity(loanId);

        if (loan.getStatus() == LoanStatus.CLOSED
                || loan.getStatus() == LoanStatus.CANCELLED
                || loan.getStatus() == LoanStatus.WRITTEN_OFF) {
            throw new IllegalStateException("Repayment is not allowed for this loan status");
        }

        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .amountPaid(request.getAmount())
                .paymentReference(request.getPaymentReference())
                .paymentChannel(request.getPaymentChannel())
                .status(RepaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        loanRepaymentRepository.save(repayment);
        loan.getRepayments().add(repayment);

        loan.setTotalPaid(loan.getTotalPaid().add(request.getAmount()));
        loan.setBalance(loan.getBalance().subtract(request.getAmount()));


        // release limit by repayment amount

        customerService.releaseLimit(loan.getCustomerId(), request.getAmount());

        if (loan.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setBalance(BigDecimal.ZERO);
            loan.setStatus(LoanStatus.CLOSED);
        } else if (loan.getStatus() == LoanStatus.DISBURSED) {
            loan.setStatus(LoanStatus.OPEN);
        }

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = getCustomerEntity(savedLoan.getCustomerId());

        log.info("Publishing repayLoan for loanId={}", savedLoan.getId());
        LoanRepaidEvent repaidEvent = new LoanRepaidEvent(savedLoan.getCustomerId(),
                savedLoan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                savedLoan.getLoanNumber(),
                request.getAmount(),
                NotificationEventType.LOAN_REPAID);
        sendToQueue(Utils.setJsonString(repaidEvent));
//        eventPublisher.publishEvent(
//                new LoanRepaidEvent(
//                        savedLoan.getCustomerId(),
//                        savedLoan.getId(),
//                        buildCustomerName(customer),
//                        customer.getPhoneNumber(),
//                        savedLoan.getLoanNumber(),
//                        request.getAmount()
//                )
//        );

        return LoanMapper.toResponse(savedLoan);
    }
    @Override
    public LoanResponse cancelLoan(Long loanId) {
        Loan loan = getLoanEntity(loanId);

        if (loan.getStatus() != LoanStatus.CREATED) {
            throw new IllegalStateException("Only CREATED loans can be cancelled");
        }

        loan.setStatus(LoanStatus.CANCELLED);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = getCustomerEntity(savedLoan.getCustomerId());

        log.info("Publishing cancelLoan for loanId={}", savedLoan.getId());
        LoanCancelledEvent loanCancelledEvent = new LoanCancelledEvent(savedLoan.getCustomerId(),
                savedLoan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                savedLoan.getLoanNumber(),
                NotificationEventType.LOAN_CANCELLED);
        sendToQueue(Utils.setJsonString(loanCancelledEvent));
//        eventPublisher.publishEvent(
//                new LoanCancelledEvent(
//                        savedLoan.getCustomerId(),
//                        savedLoan.getId(),
//                        buildCustomerName(customer),
//                        customer.getPhoneNumber(),
//                        savedLoan.getLoanNumber()
//                )
//        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanResponse writeOffLoan(Long loanId) {
        Loan loan = getLoanEntity(loanId);

        if (loan.getStatus() != LoanStatus.OPEN && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Only OPEN or OVERDUE loans can be written off");
        }

        if (loan.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Only loans with outstanding balance can be written off");
        }

        loan.setStatus(LoanStatus.WRITTEN_OFF);

        Loan savedLoan = loanRepository.save(loan);
        CustomerResponse customer = getCustomerEntity(savedLoan.getCustomerId());

        log.info("Publishing writeOffLoan for loanId={}", savedLoan.getId());
        LoanWrittenOffEvent loanWrittenOffEvent = new LoanWrittenOffEvent(savedLoan.getCustomerId(),
                savedLoan.getId(),
                buildCustomerName(customer),
                customer.getPhoneNumber(),
                savedLoan.getLoanNumber(),
                savedLoan.getBalance(),
                NotificationEventType.LOAN_WRITTEN_OFF);

        sendToQueue(Utils.setJsonString(loanWrittenOffEvent));
//        eventPublisher.publishEvent(
//                new LoanWrittenOffEvent(
//                        savedLoan.getCustomerId(),
//                        savedLoan.getId(),
//                        buildCustomerName(customer),
//                        customer.getPhoneNumber(),
//                        savedLoan.getLoanNumber(),
//                        savedLoan.getBalance()
//                )
//        );

        return LoanMapper.toResponse(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        return LoanMapper.toResponse(getLoanEntity(loanId));
    }


    @Transactional(readOnly = true)
    @Override
    public List<LoanResponse> getLoansByCustomer(Long customerId) {


        customerService.getCustomerEntity(customerId);



        List<Loan> loans = loanRepository.findByCustomerId(customerId);


        return loans.stream()
                .map(LoanMapper::toResponse)
                .toList();
    }

    @Override
    public void markOverdueLoans() throws SSLException {
        List<Loan> overdueCandidates = loanRepository.findByStatusAndDueDateBefore(LoanStatus.DISBURSED, LocalDate.now());
        overdueCandidates.addAll(loanRepository.findByStatusAndDueDateBefore(LoanStatus.OPEN, LocalDate.now()));

        for (Loan loan : overdueCandidates) {
            if (loan.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                loan.setStatus(LoanStatus.OVERDUE);

                String string= productService.getProductById(loan.getProductId());
                log.info("Response {}", string);
                ProductResponse product = (ProductResponse) Utils.setJsonStringToObject(string,ProductResponse.class);
//
//              ProductResponse product = productService.getProductById(loan.getProductId());

              feeCalculationService.applyLateFees(loan, product);
                Loan savedLoan = loanRepository.save(loan);

                CustomerResponse customer = getCustomerEntity(savedLoan.getCustomerId());

                log.info("Publishing markOverdueLoans for loanId={}", savedLoan.getId());
                eventPublisher.publishEvent(
                        new LoanOverdueEvent(
                                savedLoan.getCustomerId(),
                                savedLoan.getId(),
                                buildCustomerName(customer),
                                customer.getPhoneNumber(),
                                savedLoan.getLoanNumber(),
                                savedLoan.getBalance(),
                                NotificationEventType.LOAN_OVERDUE
                        )
                );
            }
        }
    }

    private Loan getLoanEntity(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));
    }

    private CustomerResponse getCustomerEntity(Long customerId) {
        String stringResponse = customerService.getCustomerEntity(customerId);
        CustomerResponse customer = (CustomerResponse) Utils.setJsonStringToObject(stringResponse,CustomerResponse.class);
        if (customer == null) {
            throw new EntityNotFoundException("Customer not found");
        }
        return customer;
    }

    private String buildCustomerName(CustomerResponse customer) {
        return customer.getFirstName() + " " + customer.getLastName();
    }

    private String generateLoanNumber() {
        return "LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateRequestedTenure(Integer tenure, ProductResponse product) {
        if (tenure < product.getMinTenure() || tenure > product.getMaxTenure()) {
            throw new IllegalArgumentException("Requested tenure is outside product range");
        }
    }

    private LocalDate resolveDueDate(LocalDate baseDate, Integer tenure, ProductResponse product) {
        return switch (product.getTenureType()) {
            case DAYS -> baseDate.plusDays(tenure);
            case MONTHS -> baseDate.plusMonths(tenure);
        };
    }
    private boolean sendToQueue(String sparkCentralRequest) {

        try{
            log.info("Queue name {}",destinationQueue);



            //push to queue
            jmsTemplate.send(destinationQueue, messageCreator -> {
                TextMessage message = messageCreator.createTextMessage();
                message.setText(sparkCentralRequest);
                log.info("Sending message {}","successfully written to queue");
                return message;
            });

            return true;
        }catch (Exception e) {


            return false;
        }
    }
}