package com.project.hugme.domain.checklist.repository;


import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.entity.product.HousingTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HousingTypeRepository
        extends JpaRepository<HousingType, Long> {

    Optional<HousingType> findByHousingTypeCode(
            HousingTypeCode housingTypeCode
    );
}