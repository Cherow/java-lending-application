package com.example.main.service;

import com.example.main.model.entity.Loan;
import com.example.main.model.dto.response.ProductResponse;

import java.math.BigDecimal;

public interface FeeCalculationService {
    BigDecimal applyOriginationFees(Loan loan, ProductResponse product);
    BigDecimal applyLateFees(Loan loan, ProductResponse product);
    BigDecimal applyDailyFees(Loan loan, ProductResponse product);
}