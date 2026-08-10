package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ContractType;
import com.project.hugme.domain.checklist.entity.application.PartyType;
import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.entity.product.HousingTypeCode;

public record OCRResponse(
        HousingTypeCode housingTypeCode,
        String housingTypeName,
        String contractAddress,
        ContractType contractType,
        PartyType tenantType,
        PartyType landlordType,
        Boolean fixedDateConfirmed,
        Boolean officetelResidentialMarked,
        Boolean landlordProxyContract
) {

    public static OCRResponse from(
            ApplicationInfo applicationInfo
    ) {
        HousingType housingType =
                applicationInfo.getHousingType();

        return new OCRResponse(
                housingType.getHousingTypeCode(),
                housingType.getHousingTypeName(),
                applicationInfo.getContractAddress(),
                applicationInfo.getContractType(),
                applicationInfo.getTenantType(),
                applicationInfo.getLandlordType(),
                applicationInfo.getFixedDateConfirmed(),
                applicationInfo.getOfficetelResidentialMarked(),
                applicationInfo.getLandlordProxyContract()

        );
    }
}


