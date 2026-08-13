package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.entity.application.ContractType;
import com.project.hugme.domain.checklist.entity.application.PartyType;
import com.project.hugme.domain.checklist.entity.product.HousingTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OCRUpdateRequest(

        @NotNull
        HousingTypeCode housingTypeCode,

        @NotBlank
        String contractAddress,

        @NotNull
        ContractType contractType,

        @NotNull
        PartyType tenantType,

        @NotNull
        PartyType landlordType,

        @NotNull
        Boolean fixedDateConfirmed,

        @NotNull
        Boolean officetelResidentialMarked,

        @NotNull
        Boolean landlordProxyContract
) {
}