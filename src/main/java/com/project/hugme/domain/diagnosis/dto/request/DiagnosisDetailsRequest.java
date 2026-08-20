package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DiagnosisDetailsRequest(
        @Size(min = 1, max = 100) String dongName,
        @Size(max = 100) String hoName,
        @NotNull @Positive Long deposit,
        @NotNull LocalDate contractDate,
        @DecimalMin("0.01") BigDecimal contractArea,
        @DecimalMin("0.01") BigDecimal exclusiveArea,

        /**
         * 단독·다가구 모델에는 층 Feature가 없어 값이 없어도 분석할 수 있다.
         * 공동주택에서 층이 비면 AI가 기본값으로 채우고 시세 신뢰도를 낮춘다.
         */
        Integer floor,

        @Size(max = 100) String landlordName
) {}
