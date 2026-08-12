package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.entity.product.ProductCode;
import jakarta.validation.constraints.NotNull;

public record ApplicationCreateRequest(
        @NotNull(message = "상품 코드는 필수입니다.")
        ProductCode productCode
) {


}
