package com.project.hugme.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="products")
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "product_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private ProductCode productCode;

    @Column(
            name = "product_name",
            nullable = false,
            length = 100
    )
    private String productName;

}
