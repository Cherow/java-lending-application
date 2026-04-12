package com.example.main.loanManagement;

import com.example.main.common.CustomerClient;
import com.example.main.common.ProductClient;
import com.example.main.common.Utils;
import com.example.main.model.dto.request.CreateLoanRequest;
import com.example.main.model.dto.request.RepayLoanRequest;
import com.example.main.model.dto.response.CustomerResponse;
import com.example.main.model.dto.response.LoanResponse;
import com.example.main.model.dto.response.ProductFeeResponse;
import com.example.main.model.dto.response.ProductResponse;
import com.example.main.model.entity.Loan;
import com.example.main.model.entity.LoanRepayment;
import com.example.main.model.enums.*;
import com.example.main.model.events.LoanCreatedEvent;
import com.example.main.model.events.LoanDisbursedEvent;
import com.example.main.model.events.LoanRepaidEvent;
import com.example.main.repository.LoanRepaymentRepository;
import com.example.main.repository.LoanRepository;
import com.example.main.service.FeeCalculationService;
import com.example.main.serviceImpl.LoanServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanRepaymentRepository loanRepaymentRepository;

    @Mock
    private FeeCalculationService feeCalculationService;

    @Mock
    private CustomerClient customerService;

    @Mock
    private ProductClient productService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LoanServiceImpl loanService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void createLoan_shouldCreateLoanApplyOriginationFeeAndPublishEvent() throws Exception {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setCustomerId(6L);
        request.setProductId(9L);
        request.setPrincipalAmount(BigDecimal.valueOf(5000));
        request.setTenure(12);

        ProductResponse product = ProductResponse.builder()
                .id(9L)
                .code("PERSONAL_LOAN")
                .name("Salary Advance")
                .description("Short-term loan")
                .tenureType(TenureType.MONTHS)
                .minTenure(1)
                .maxTenure(24)
                .active(true)
                .fixedTenureAllowed(true)
                .flexibleTenureAllowed(true)
                .fees(List.of(
                        ProductFeeResponse.builder()
                                .id(10L)
                                .feeName("Origination Fee")
                                .feeType(FeeType.DAILY)
                                .calculationType(CalculationType.FIXED)
                                .applicationStage(ApplicationStage.ORIGINATION)
                                .amount(BigDecimal.valueOf(100))
                                .active(true)
                                .build()
                ))
                .build();

        CustomerResponse customer = CustomerResponse.builder()
                .id(6L)
                .firstName("Mercy")
                .lastName("Cherotich")
                .phoneNumber("+254700111222")
                .build();

        String productJson = objectMapper.writeValueAsString(product);
        String customerJson = objectMapper.writeValueAsString(customer);

        Loan firstSaved = Loan.builder()
                .id(100L)
                .loanNumber("LN-ABC12345")
                .customerId(6L)
                .productId(9L)
                .principalAmount(BigDecimal.valueOf(5000))
                .disbursedAmount(BigDecimal.ZERO)
                .totalFees(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(5000))
                .tenure(12)
                .dueDate(LocalDate.now().plusMonths(12))
                .status(LoanStatus.CREATED)
                .fees(new ArrayList<>())
                .repayments(new ArrayList<>())
                .build();

        Loan secondSaved = Loan.builder()
                .id(100L)
                .loanNumber("LN-ABC12345")
                .customerId(6L)
                .productId(9L)
                .principalAmount(BigDecimal.valueOf(5000))
                .disbursedAmount(BigDecimal.ZERO)
                .totalFees(BigDecimal.valueOf(100))
                .totalPaid(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(5100))
                .tenure(12)
                .dueDate(LocalDate.now().plusMonths(12))
                .status(LoanStatus.CREATED)
                .fees(new ArrayList<>())
                .repayments(new ArrayList<>())
                .build();

        when(productService.getProductById(9L)).thenReturn(productJson);
        when(customerService.getCustomerEntity(6L)).thenReturn(customerJson);
        when(loanRepository.save(any(Loan.class))).thenReturn(firstSaved, secondSaved);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.setJsonString(any())).thenReturn("json");
            utils.when(() -> Utils.setJsonStringToObject(customerJson, CustomerResponse.class))
                    .thenReturn(customer);

            LoanResponse response = loanService.createLoan(request);

            assertNotNull(response);
            assertEquals(100L, response.getId());

            verify(customerService).validateCustomerForLoan(6L, BigDecimal.valueOf(5000));
            verify(feeCalculationService).applyOriginationFees(firstSaved, product);
            verify(loanRepository, times(2)).save(any(Loan.class));

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertTrue(eventCaptor.getValue() instanceof LoanCreatedEvent);
        }
    }

    @Test
    void createLoan_shouldThrowWhenTenureOutsideProductRange() throws Exception {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setCustomerId(6L);
        request.setProductId(9L);
        request.setPrincipalAmount(BigDecimal.valueOf(5000));
        request.setTenure(50);

        ProductResponse product = ProductResponse.builder()
                .id(9L)
                .tenureType(TenureType.MONTHS)
                .minTenure(1)
                .maxTenure(24)
                .build();

        CustomerResponse customer = CustomerResponse.builder()
                .id(6L)
                .firstName("Mercy")
                .lastName("Cherotich")
                .phoneNumber("+254700111222")
                .build();

        String productJson = objectMapper.writeValueAsString(product);
        String customerJson = objectMapper.writeValueAsString(customer);

        when(productService.getProductById(9L)).thenReturn(productJson);
        when(customerService.getCustomerEntity(6L)).thenReturn(customerJson);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.setJsonString(any())).thenReturn("json");
            utils.when(() -> Utils.setJsonStringToObject(customerJson, CustomerResponse.class))
                    .thenReturn(customer);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> loanService.createLoan(request)
            );

            assertEquals("Requested tenure is outside product range", ex.getMessage());
            verify(customerService, never()).validateCustomerForLoan(anyLong(), any());
            verify(loanRepository, never()).save(any());
        }
    }

    @Test
    void disburseLoan_shouldConsumeLimitUpdateLoanAndPublishEvent() {
        Loan loan = Loan.builder()
                .id(1L)
                .loanNumber("LN-001")
                .customerId(6L)
                .productId(9L)
                .principalAmount(BigDecimal.valueOf(5000))
                .balance(BigDecimal.valueOf(5000))
                .status(LoanStatus.CREATED)
                .fees(new ArrayList<>())
                .repayments(new ArrayList<>())
                .build();

        CustomerResponse customer = CustomerResponse.builder()
                .id(6L)
                .firstName("Mercy")
                .lastName("Cherotich")
                .phoneNumber("+254700111222")
                .build();

        String customerJson = """
                {"id":6,"firstName":"Mercy","lastName":"Cherotich","phoneNumber":"+254700111222"}
                """;

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.getCustomerEntity(6L)).thenReturn(customerJson);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.setJsonStringToObject(customerJson, CustomerResponse.class))
                    .thenReturn(customer);

            LoanResponse response = loanService.disburseLoan(1L);

            assertNotNull(response);
            verify(customerService).consumeLimit(6L, BigDecimal.valueOf(5000));
            assertEquals(LoanStatus.DISBURSED, loan.getStatus());
            assertEquals(BigDecimal.valueOf(5000), loan.getDisbursedAmount());
            assertEquals(LocalDate.now(), loan.getDisbursementDate());

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertTrue(eventCaptor.getValue() instanceof LoanDisbursedEvent);
        }
    }

    @Test
    void repayLoan_shouldCloseLoanWhenBalanceBecomesZero() {
        Loan loan = Loan.builder()
                .id(1L)
                .loanNumber("LN-001")
                .customerId(6L)
                .principalAmount(BigDecimal.valueOf(5000))
                .totalPaid(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(1000))
                .status(LoanStatus.OPEN)
                .repayments(new ArrayList<>())
                .fees(new ArrayList<>())
                .build();

        RepayLoanRequest request = new RepayLoanRequest();
        request.setAmount(BigDecimal.valueOf(1000));
        request.setPaymentReference("REF123");
        request.setPaymentChannel("MPESA");

        CustomerResponse customer = CustomerResponse.builder()
                .id(6L)
                .firstName("Mercy")
                .lastName("Cherotich")
                .phoneNumber("+254700111222")
                .build();

        String customerJson = """
                {"id":6,"firstName":"Mercy","lastName":"Cherotich","phoneNumber":"+254700111222"}
                """;

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.getCustomerEntity(6L)).thenReturn(customerJson);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.setJsonStringToObject(customerJson, CustomerResponse.class))
                    .thenReturn(customer);

            LoanResponse response = loanService.repayLoan(1L, request);

            assertNotNull(response);
            verify(loanRepaymentRepository).save(any(LoanRepayment.class));
            verify(customerService).releaseLimit(6L, BigDecimal.valueOf(1000));
            assertEquals(BigDecimal.ZERO, loan.getBalance());
            assertEquals(LoanStatus.CLOSED, loan.getStatus());
            assertEquals(BigDecimal.valueOf(1000), loan.getTotalPaid());

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertTrue(eventCaptor.getValue() instanceof LoanRepaidEvent);
        }
    }

    @Test
    void cancelLoan_shouldSetStatusToCancelled() {
        Loan loan = Loan.builder()
                .id(1L)
                .status(LoanStatus.CREATED)
                .repayments(new ArrayList<>())
                .fees(new ArrayList<>())
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse response = loanService.cancelLoan(1L);

        assertNotNull(response);
        assertEquals(LoanStatus.CANCELLED, loan.getStatus());
        verify(loanRepository).save(loan);
    }

    @Test
    void writeOffLoan_shouldThrowWhenBalanceIsZero() {
        Loan loan = Loan.builder()
                .id(1L)
                .status(LoanStatus.OPEN)
                .balance(BigDecimal.ZERO)
                .repayments(new ArrayList<>())
                .fees(new ArrayList<>())
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> loanService.writeOffLoan(1L)
        );

        assertEquals("Only loans with outstanding balance can be written off", ex.getMessage());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void getLoanById_shouldThrowWhenLoanMissing() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> loanService.getLoanById(99L));
    }

    @Test
    void getLoansByCustomer_shouldReturnMappedLoans() {
        Loan loan1 = Loan.builder()
                .id(1L)
                .customerId(6L)
                .status(LoanStatus.CREATED)
                .principalAmount(BigDecimal.valueOf(1000))
                .totalFees(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(1000))
                .repayments(new ArrayList<>())
                .fees(new ArrayList<>())
                .build();

        Loan loan2 = Loan.builder()
                .id(2L)
                .customerId(6L)
                .status(LoanStatus.OPEN)
                .principalAmount(BigDecimal.valueOf(2000))
                .totalFees(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .balance(BigDecimal.valueOf(1500))
                .repayments(new ArrayList<>())
                .fees(new ArrayList<>())
                .build();

        String customerJson = """
                {"id":6,"firstName":"Mercy","lastName":"Cherotich","phoneNumber":"+254700111222"}
                """;

        CustomerResponse customer = CustomerResponse.builder()
                .id(6L)
                .firstName("Mercy")
                .lastName("Cherotich")
                .phoneNumber("+254700111222")
                .build();

        when(customerService.getCustomerEntity(6L)).thenReturn(customerJson);
        when(loanRepository.findByCustomerId(6L)).thenReturn(List.of(loan1, loan2));

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(() -> Utils.setJsonStringToObject(customerJson, CustomerResponse.class))
                    .thenReturn(customer);

            List<LoanResponse> responses = loanService.getLoansByCustomer(6L);

            assertEquals(2, responses.size());
            verify(loanRepository).findByCustomerId(6L);
        }
    }
}