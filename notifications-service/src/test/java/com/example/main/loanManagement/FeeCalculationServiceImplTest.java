package com.example.main.loanManagement;

import com.example.main.loanManagement.model.dto.response.ProductFeeResponse;
import com.example.main.loanManagement.model.dto.response.ProductResponse;
import com.example.main.loanManagement.model.entity.Loan;
import com.example.main.loanManagement.model.entity.LoanFee;
import com.example.main.loanManagement.model.enums.LoanFeeType;
import com.example.main.loanManagement.repository.LoanFeeRepository;
import com.example.main.loanManagement.service.serviceImpl.FeeCalculationServiceImpl;
import com.example.main.loanManagement.model.enums.ApplicationStage;
import com.example.main.loanManagement.model.enums.CalculationType;
import com.example.main.loanManagement.model.enums.FeeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceImplTest {

    @Mock
    private LoanFeeRepository loanFeeRepository;

    @InjectMocks
    private FeeCalculationServiceImpl feeCalculationService;

    private Loan loan;
    private ProductResponse product;

    @BeforeEach
    void setUp() {
        loan = Loan.builder()
                .id(1L)
                .principalAmount(new BigDecimal("10000"))
                .totalFees(BigDecimal.ZERO)
                .balance(new BigDecimal("10000"))
                .fees(new ArrayList<>())
                .build();

        product = ProductResponse.builder()
                .id(1L)
                .fees(new ArrayList<>())
                .build();
    }

    @Test
    void applyOriginationFees_shouldApplyFixedOriginationFee() {
        ProductFeeResponse productFee = ProductFeeResponse.builder()
                .feeName("Service Fee")
                .feeType(FeeType.SERVICE)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("500"))
                .applicationStage(ApplicationStage.ORIGINATION)
                .active(true)
                .build();

        product.setFees(List.of(productFee));

        when(loanFeeRepository.save(any(LoanFee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = feeCalculationService.applyOriginationFees(loan, product);

        assertEquals(new BigDecimal("500"), total);
        assertEquals(new BigDecimal("500"), loan.getTotalFees());
        assertEquals(new BigDecimal("10500"), loan.getBalance());
        assertEquals(1, loan.getFees().size());

        ArgumentCaptor<LoanFee> captor = ArgumentCaptor.forClass(LoanFee.class);
        verify(loanFeeRepository).save(captor.capture());

        LoanFee savedFee = captor.getValue();
        assertEquals("Service Fee", savedFee.getFeeName());
        assertEquals(LoanFeeType.SERVICE, savedFee.getFeeType());
        assertEquals(new BigDecimal("500"), savedFee.getAmount());
        assertEquals("Origination fee applied", savedFee.getReason());
        assertNotNull(savedFee.getAppliedAt());
    }

    @Test
    void applyLateFees_shouldApplyPercentageLateFee() {
        ProductFeeResponse productFee = ProductFeeResponse.builder()
                .feeName("Late Fee")
                .feeType(FeeType.LATE)
                .calculationType(CalculationType.PERCENTAGE)
                .percentage(new BigDecimal("10"))
                .applicationStage(ApplicationStage.LATE_PAYMENT)
                .active(true)
                .build();

        product.setFees(List.of(productFee));

        when(loanFeeRepository.save(any(LoanFee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = feeCalculationService.applyLateFees(loan, product);

        assertEquals(new BigDecimal("1000.00"), total);
        assertEquals(new BigDecimal("1000.00"), loan.getTotalFees());
        assertEquals(new BigDecimal("11000.00"), loan.getBalance());
        assertEquals(1, loan.getFees().size());

        LoanFee loanFee = loan.getFees().get(0);
        assertEquals("Late Fee", loanFee.getFeeName());
        assertEquals(LoanFeeType.LATE, loanFee.getFeeType());
        assertEquals("Late fee applied", loanFee.getReason());
    }

    @Test
    void applyDailyFees_shouldApplyOnlyActiveMatchingStageFees() {
        ProductFeeResponse dailyFee1 = ProductFeeResponse.builder()
                .feeName("Daily Fee 1")
                .feeType(FeeType.DAILY)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("100"))
                .applicationStage(ApplicationStage.DAILY_ACCRUAL)
                .active(true)
                .build();

        ProductFeeResponse dailyFee2 = ProductFeeResponse.builder()
                .feeName("Daily Fee 2")
                .feeType(FeeType.DAILY)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("200"))
                .applicationStage(ApplicationStage.DAILY_ACCRUAL)
                .active(true)
                .build();

        ProductFeeResponse wrongStageFee = ProductFeeResponse.builder()
                .feeName("Origination Fee")
                .feeType(FeeType.SERVICE)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("500"))
                .applicationStage(ApplicationStage.ORIGINATION)
                .active(true)
                .build();

        ProductFeeResponse inactiveFee = ProductFeeResponse.builder()
                .feeName("Inactive Daily Fee")
                .feeType(FeeType.DAILY)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("300"))
                .applicationStage(ApplicationStage.DAILY_ACCRUAL)
                .active(false)
                .build();

        product.setFees(List.of(dailyFee1, dailyFee2, wrongStageFee, inactiveFee));

        when(loanFeeRepository.save(any(LoanFee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = feeCalculationService.applyDailyFees(loan, product);

        assertEquals(new BigDecimal("300"), total);
        assertEquals(new BigDecimal("300"), loan.getTotalFees());
        assertEquals(new BigDecimal("10300"), loan.getBalance());
        assertEquals(2, loan.getFees().size());

        verify(loanFeeRepository, times(2)).save(any(LoanFee.class));
    }

    @Test
    void applyOriginationFees_shouldInitializeLoanFeesWhenNull() {
        loan.setFees(null);

        ProductFeeResponse productFee = ProductFeeResponse.builder()
                .feeName("Service Fee")
                .feeType(FeeType.SERVICE)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("250"))
                .applicationStage(ApplicationStage.ORIGINATION)
                .active(true)
                .build();

        product.setFees(List.of(productFee));

        when(loanFeeRepository.save(any(LoanFee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = feeCalculationService.applyOriginationFees(loan, product);

        assertNotNull(loan.getFees());
        assertEquals(1, loan.getFees().size());
        assertEquals(new BigDecimal("250"), total);
        assertEquals(new BigDecimal("250"), loan.getTotalFees());
        assertEquals(new BigDecimal("10250"), loan.getBalance());
    }

    @Test
    void applyOriginationFees_shouldReturnZeroWhenNoMatchingFees() {
        ProductFeeResponse productFee = ProductFeeResponse.builder()
                .feeName("Late Fee")
                .feeType(FeeType.LATE)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("400"))
                .applicationStage(ApplicationStage.LATE_PAYMENT)
                .active(true)
                .build();

        product.setFees(List.of(productFee));

        BigDecimal total = feeCalculationService.applyOriginationFees(loan, product);

        assertEquals(BigDecimal.ZERO, total);
        assertEquals(BigDecimal.ZERO, loan.getTotalFees());
        assertEquals(new BigDecimal("10000"), loan.getBalance());
        assertTrue(loan.getFees().isEmpty());

        verify(loanFeeRepository, never()).save(any(LoanFee.class));
    }

    @Test
    void applyLateFees_shouldRoundPercentageFeeToTwoDecimalPlaces() {
        loan.setPrincipalAmount(new BigDecimal("9999"));

        ProductFeeResponse productFee = ProductFeeResponse.builder()
                .feeName("Late Fee")
                .feeType(FeeType.LATE)
                .calculationType(CalculationType.PERCENTAGE)
                .percentage(new BigDecimal("2.5"))
                .applicationStage(ApplicationStage.LATE_PAYMENT)
                .active(true)
                .build();

        product.setFees(List.of(productFee));

        when(loanFeeRepository.save(any(LoanFee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal total = feeCalculationService.applyLateFees(loan, product);

        assertEquals(new BigDecimal("249.98"), total);
        assertEquals(new BigDecimal("249.98"), loan.getTotalFees());
        assertEquals(new BigDecimal("10249.98"), loan.getBalance());
    }
}