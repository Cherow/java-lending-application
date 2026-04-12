package com.example.main.customerModule;

import com.example.main.model.dto.CreateCustomerRequest;
import com.example.main.model.dto.CustomerLimitResponse;
import com.example.main.model.dto.CustomerResponse;
import com.example.main.model.dto.SetCustomerLimitRequest;
import com.example.main.model.dto.UpdateCustomerRequest;
import com.example.main.model.entity.Customer;
import com.example.main.model.entity.CustomerLimit;
import com.example.main.model.enums.CustomerStatus;
import com.example.main.repository.CustomerLimitRepository;
import com.example.main.repository.CustomerRepository;
import com.example.main.service.serviceImpl.CustomerServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerLimitRepository customerLimitRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerLimit customerLimit;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .customerNumber("CUST-12345678")
                .firstName("Mercy")
                .lastName("Cherotich")
                .phoneNumber("0712345678")
                .email("mercy@test.com")
                .nationalId("12345678")
                .status(CustomerStatus.ACTIVE)
                .build();

        customerLimit = CustomerLimit.builder()
                .id(1L)
                .customerId(1L)
                .maxLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("8000"))
                .utilizedLimit(new BigDecimal("2000"))
                .effectiveFrom(LocalDate.now())
                .effectiveTo(LocalDate.now().plusMonths(1))
                .build();
    }

    @Test
    void createCustomer_shouldCreateCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Mercy");
        request.setLastName("Cherotich");
        request.setPhoneNumber("0712345678");
        request.setEmail("mercy@test.com");
        request.setNationalId("12345678");

        when(customerRepository.existsByPhoneNumber("0712345678")).thenReturn(false);
        when(customerRepository.existsByNationalId("12345678")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Mercy", response.getFirstName());
        assertEquals("Cherotich", response.getLastName());
        assertEquals("0712345678", response.getPhoneNumber());
        assertEquals("12345678", response.getNationalId());

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_shouldThrowExceptionWhenPhoneNumberExists() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setPhoneNumber("0712345678");
        request.setNationalId("12345678");

        when(customerRepository.existsByPhoneNumber("0712345678")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertEquals("Phone number already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_shouldThrowExceptionWhenNationalIdExists() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setPhoneNumber("0712345678");
        request.setNationalId("12345678");

        when(customerRepository.existsByPhoneNumber("0712345678")).thenReturn(false);
        when(customerRepository.existsByNationalId("12345678")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertEquals("National ID already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_shouldUpdateCustomerSuccessfully() {
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setPhoneNumber("0799999999");
        request.setEmail("jane@test.com");
        request.setStatus(CustomerStatus.INACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.updateCustomer(1L, request);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("0799999999", response.getPhoneNumber());
        assertEquals("jane@test.com", response.getEmail());
        assertEquals(CustomerStatus.INACTIVE, response.getStatus());
    }

    @Test
    void getCustomerById_shouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Mercy", response.getFirstName());
    }

    @Test
    void getCustomerById_shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> customerService.getCustomerById(1L)
        );

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void setCustomerLimit_shouldCreateNewLimitWhenNoneExists() {
        SetCustomerLimitRequest request = new SetCustomerLimitRequest();
        request.setMaxLimit(new BigDecimal("15000"));
        request.setEffectiveFrom(LocalDate.now());
        request.setEffectiveTo(LocalDate.now().plusMonths(1));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.empty());
        when(customerLimitRepository.save(any(CustomerLimit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerLimitResponse response = customerService.setCustomerLimit(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("15000"), response.getMaxLimit());
        assertEquals(new BigDecimal("15000"), response.getAvailableLimit());
        assertEquals(BigDecimal.ZERO, response.getUtilizedLimit());
    }

    @Test
    void setCustomerLimit_shouldUpdateExistingLimit() {
        SetCustomerLimitRequest request = new SetCustomerLimitRequest();
        request.setMaxLimit(new BigDecimal("12000"));
        request.setEffectiveFrom(LocalDate.now());
        request.setEffectiveTo(LocalDate.now().plusMonths(2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));
        when(customerLimitRepository.save(any(CustomerLimit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerLimitResponse response = customerService.setCustomerLimit(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("12000"), response.getMaxLimit());
        assertEquals(new BigDecimal("10000"), response.getAvailableLimit()); // 12000 - 2000
    }

    @Test
    void getCustomerLimit_shouldReturnLimit() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));

        CustomerLimitResponse response = customerService.getCustomerLimit(1L);

        assertNotNull(response);
        assertEquals(new BigDecimal("10000"), response.getMaxLimit());
    }

    @Test
    void getCustomerLimit_shouldThrowWhenLimitNotFound() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> customerService.getCustomerLimit(1L)
        );

        assertEquals("Customer limit not found", exception.getMessage());
    }

    @Test
    void validateCustomerForLoan_shouldPassWhenCustomerIsActiveAndLimitIsEnough() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));

        assertDoesNotThrow(() ->
                customerService.validateCustomerForLoan(1L, new BigDecimal("5000"))
        );
    }

    @Test
    void validateCustomerForLoan_shouldThrowWhenCustomerIsNotActive() {
        customer.setStatus(CustomerStatus.INACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> customerService.validateCustomerForLoan(1L, new BigDecimal("5000"))
        );

        assertEquals("Customer is not eligible for borrowing", exception.getMessage());
    }

    @Test
    void validateCustomerForLoan_shouldThrowWhenLimitIsInsufficient() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.validateCustomerForLoan(1L, new BigDecimal("9000"))
        );

        assertEquals("Insufficient available limit", exception.getMessage());
    }

    @Test
    void consumeLimit_shouldUpdateAvailableAndUtilizedLimit() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));

        customerService.consumeLimit(1L, new BigDecimal("1000"));

        assertEquals(new BigDecimal("7000"), customerLimit.getAvailableLimit());
        assertEquals(new BigDecimal("3000"), customerLimit.getUtilizedLimit());

        verify(customerLimitRepository).save(customerLimit);
    }

    @Test
    void consumeLimit_shouldThrowWhenAvailableLimitIsInsufficient() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.consumeLimit(1L, new BigDecimal("9000"))
        );

        assertEquals("Insufficient available limit", exception.getMessage());
        verify(customerLimitRepository, never()).save(any(CustomerLimit.class));
    }

    @Test
    void releaseLimit_shouldUpdateAvailableAndUtilizedLimit() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.of(customerLimit));

        customerService.releaseLimit(1L, new BigDecimal("1000"));

        assertEquals(new BigDecimal("9000"), customerLimit.getAvailableLimit());
        assertEquals(new BigDecimal("1000"), customerLimit.getUtilizedLimit());

        verify(customerLimitRepository).save(customerLimit);
    }

    @Test
    void consumeLimit_shouldThrowWhenLimitNotFound() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> customerService.consumeLimit(1L, new BigDecimal("1000"))
        );

        assertEquals("Customer limit not found", exception.getMessage());
    }

    @Test
    void releaseLimit_shouldThrowWhenLimitNotFound() {
        when(customerLimitRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> customerService.releaseLimit(1L, new BigDecimal("1000"))
        );

        assertEquals("Customer limit not found", exception.getMessage());
    }
}