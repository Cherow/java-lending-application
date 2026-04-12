package com.example.main.repository;

import com.example.main.model.entity.ProductFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductFeeRepository extends JpaRepository<ProductFee, Long> {
    List<ProductFee> findByProductId(Long productId);
}