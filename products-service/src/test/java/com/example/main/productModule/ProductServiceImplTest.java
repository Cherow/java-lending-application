package com.example.main.productModule;

import com.example.main.exception.BusinessException;
import com.example.main.model.dto.AddProductFeeRequest;
import com.example.main.model.dto.CreateProductRequest;
import com.example.main.model.dto.ProductFeeResponse;
import com.example.main.model.dto.ProductResponse;
import com.example.main.model.dto.UpdateProductRequest;
import com.example.main.model.entity.Product;
import com.example.main.model.entity.ProductFee;
import com.example.main.model.enums.CalculationType;
import com.example.main.model.enums.FeeType;
import com.example.main.model.enums.TenureType;
import com.example.main.repository.ProductFeeRepository;
import com.example.main.repository.ProductRepository;
import com.example.main.service.serviceImpl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductFeeRepository productFeeRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldCreateProductSuccessfully() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("PRD001");
        request.setName("Personal Loan");
        request.setDescription("Personal loan product");
        request.setTenureType(TenureType.DAYS);
        request.setMinTenure(7);
        request.setMaxTenure(30);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(false);

        Product savedProduct = Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Personal Loan")
                .description("Personal loan product")
                .tenureType(TenureType.DAYS)
                .minTenure(7)
                .maxTenure(30)
                .fixedTenureAllowed(true)
                .flexibleTenureAllowed(false)
                .active(true)
                .build();

        when(productRepository.existsByCode("PRD001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("PRD001", response.getCode());
        assertEquals("Personal Loan", response.getName());
        assertTrue(response.getActive());

        verify(productRepository).existsByCode("PRD001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenCodeExists() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("PRD001");
        request.setMinTenure(7);
        request.setMaxTenure(30);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(false);

        when(productRepository.existsByCode("PRD001")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Product code already exists", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenMinTenureIsNull() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("PRD001");
        request.setMinTenure(null);
        request.setMaxTenure(30);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Min and Max tenure must not be null", exception.getMessage());
    }

    @Test
    void createProduct_shouldThrowExceptionWhenTenureIsLessThanOrEqualToZero() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("PRD001");
        request.setMinTenure(0);
        request.setMaxTenure(30);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Tenure must be greater than zero", exception.getMessage());
    }

    @Test
    void createProduct_shouldThrowExceptionWhenMinTenureGreaterThanMaxTenure() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("PRD001");
        request.setMinTenure(40);
        request.setMaxTenure(30);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Minimum tenure cannot be greater than maximum tenure", exception.getMessage());
    }

    @Test
    void createProduct_shouldThrowExceptionWhenBothTenureModesAreDisabled() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCode("PRD001");
        request.setMinTenure(7);
        request.setMaxTenure(30);
        request.setFixedTenureAllowed(false);
        request.setFlexibleTenureAllowed(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("At least one tenure mode (fixed or flexible) must be enabled", exception.getMessage());
    }

    @Test
    void updateProduct_shouldUpdateSuccessfully() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Loan");
        request.setDescription("Updated description");
        request.setTenureType(TenureType.DAYS);
        request.setMinTenure(10);
        request.setMaxTenure(40);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(true);
        request.setActive(false);

        Product existingProduct = Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Old Name")
                .description("Old description")
                .tenureType(TenureType.DAYS)
                .minTenure(7)
                .maxTenure(30)
                .fixedTenureAllowed(true)
                .flexibleTenureAllowed(false)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.updateProduct(1L, request);

        assertNotNull(response);
        assertEquals("Updated Loan", response.getName());
        assertEquals("Updated description", response.getDescription());
        assertEquals(10, response.getMinTenure());
        assertEquals(40, response.getMaxTenure());
        assertFalse(response.getActive());
    }

    @Test
    void updateProduct_shouldThrowWhenProductNotFound() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setMinTenure(10);
        request.setMaxTenure(40);
        request.setFixedTenureAllowed(true);
        request.setFlexibleTenureAllowed(false);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> productService.updateProduct(1L, request)
        );

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void getProductById_shouldReturnProduct() {
        Product product = Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Loan Product")
                .description("Test product")
                .tenureType(TenureType.DAYS)
                .minTenure(7)
                .maxTenure(30)
                .fixedTenureAllowed(true)
                .flexibleTenureAllowed(false)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PRD001", response.getCode());
    }

    @Test
    void getProductById_shouldThrowWhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> productService.getProductById(1L)
        );

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        Product product1 = Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Loan 1")
                .active(true)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .code("PRD002")
                .name("Loan 2")
                .active(true)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<ProductResponse> responses = productService.getAllProducts();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("PRD001", responses.get(0).getCode());
        assertEquals("PRD002", responses.get(1).getCode());
    }

    @Test
    void addFee_shouldAddFixedFeeSuccessfully() {
        Product product = Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Loan Product")
                .build();

        AddProductFeeRequest request = new AddProductFeeRequest();
        request.setFeeName("Processing Fee");
        request.setFeeType(FeeType.LATE);
        request.setCalculationType(CalculationType.FIXED);
        request.setAmount(new BigDecimal("500"));
        request.setPercentage(new BigDecimal("10"));
        request.setDaysAfterDue(3); // required for LATE
        request.setActive(true);

        ProductFee savedFee = ProductFee.builder()
                .id(1L)
                .product(product)
                .feeName("Processing Fee")
                .feeType(FeeType.LATE)
                .calculationType(CalculationType.FIXED)
                .amount(new BigDecimal("500"))
                .percentage(null)
                .daysAfterDue(3)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productFeeRepository.save(any(ProductFee.class))).thenReturn(savedFee);

        ProductFeeResponse response = productService.addFee(1L, request);

        assertNotNull(response);
        assertEquals("Processing Fee", response.getFeeName());
        assertEquals(CalculationType.FIXED, response.getCalculationType());
        assertEquals(new BigDecimal("500"), response.getAmount());
        assertNull(request.getPercentage());
    }

    @Test
    void addFee_shouldAddPercentageFeeSuccessfully() {
        Product product = Product.builder()
                .id(1L)
                .code("PRD001")
                .name("Loan Product")
                .build();

        AddProductFeeRequest request = new AddProductFeeRequest();
        request.setFeeName("Interest Fee");
        request.setFeeType(FeeType.SERVICE);
        request.setCalculationType(CalculationType.PERCENTAGE);
        request.setPercentage(new BigDecimal("12.5"));
        request.setAmount(new BigDecimal("500"));
        request.setActive(true);

        ProductFee savedFee = ProductFee.builder()
                .id(1L)
                .product(product)
                .feeName("Interest Fee")
                .feeType(FeeType.DAILY)
                .calculationType(CalculationType.PERCENTAGE)
                .percentage(new BigDecimal("12.5"))
                .amount(null)
                .active(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productFeeRepository.save(any(ProductFee.class))).thenReturn(savedFee);

        ProductFeeResponse response = productService.addFee(1L, request);

        assertNotNull(response);
        assertEquals("Interest Fee", response.getFeeName());
        assertEquals(CalculationType.PERCENTAGE, response.getCalculationType());
        assertEquals(new BigDecimal("12.5"), response.getPercentage());
        assertNull(request.getAmount());
    }

    @Test
    void addFee_shouldThrowWhenProductNotFound() {
        AddProductFeeRequest request = new AddProductFeeRequest();
        request.setCalculationType(CalculationType.FIXED);
        request.setAmount(new BigDecimal("500"));
        request.setFeeType(FeeType.SERVICE);

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> productService.addFee(1L, request)
        );

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void addFee_shouldThrowWhenFixedFeeAmountIsMissing() {
        Product product = Product.builder().id(1L).build();

        AddProductFeeRequest request = new AddProductFeeRequest();
        request.setFeeType(FeeType.SERVICE);
        request.setCalculationType(CalculationType.FIXED);
        request.setAmount(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addFee(1L, request)
        );

        assertEquals("Amount is required for FIXED fee", exception.getMessage());
    }

    @Test
    void addFee_shouldThrowWhenPercentageFeePercentageIsMissing() {
        Product product = Product.builder().id(1L).build();

        AddProductFeeRequest request = new AddProductFeeRequest();
        request.setFeeType(FeeType.SERVICE);
        request.setCalculationType(CalculationType.PERCENTAGE);
        request.setPercentage(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addFee(1L, request)
        );

        assertEquals("Percentage is required for PERCENTAGE fee", exception.getMessage());
    }

    @Test
    void addFee_shouldThrowWhenLateFeeDaysAfterDueIsMissing() {
        Product product = Product.builder().id(1L).build();

        AddProductFeeRequest request = new AddProductFeeRequest();
        request.setFeeType(FeeType.LATE);
        request.setCalculationType(CalculationType.FIXED);
        request.setAmount(new BigDecimal("300"));
        request.setDaysAfterDue(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addFee(1L, request)
        );

        assertEquals("daysAfterDue is required for late fees", exception.getMessage());
    }
}