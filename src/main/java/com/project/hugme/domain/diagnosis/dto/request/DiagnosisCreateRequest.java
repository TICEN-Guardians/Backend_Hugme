package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record DiagnosisCreateRequest(

        @NotBlank
        @Size(max = 500)
        String address,

        @Size(min = 1, max = 100)
        String dongName,

        @Size(max = 100)
        String hoName,

        @Positive
        Long deposit,

        LocalDate contractDate,

        @DecimalMin("0.01")
        BigDecimal contractArea,

        @DecimalMin("0.01")
        BigDecimal exclusiveArea,

        Integer floor,

        @Size(max = 100)
        String landlordName,

        /**
         * properties/resolve 응답에 실려 온 값을 그대로 다시 보낸다.
         * 없으면 분석 단계에서 주소·건축물대장을 다시 조회하므로 선택 값이다.
         */
        Map<String, Object> propertySnapshot
) {
}
