package com.project.hugme.domain.checklist.entity.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "housing_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HousingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "housing_type_id")
    private Long housingTypeId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "housing_type_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private HousingTypeCode housingTypeCode;

    @Column(
            name = "housing_type_name",
            nullable = false,
            length = 100
    )
    private String housingTypeName;
}
