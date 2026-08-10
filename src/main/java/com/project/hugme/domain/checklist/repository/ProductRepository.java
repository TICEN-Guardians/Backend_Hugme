package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.Product;
import com.project.hugme.domain.checklist.entity.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(ProductCode productCode);
}
