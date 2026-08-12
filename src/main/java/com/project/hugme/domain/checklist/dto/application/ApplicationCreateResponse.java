package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationStatus;
import com.project.hugme.domain.checklist.entity.product.ProductCode;

public record ApplicationCreateResponse(
        Long applicationId,
        ProductCode productCode,
        ApplicationStatus applicationStatus
) {

    public static ApplicationCreateResponse from(
            Application application
    ) {
        return new ApplicationCreateResponse(
                application.getApplicationId(),
                application.getProduct().getProductCode(),
                application.getApplicationStatus()
        );
    }
}
