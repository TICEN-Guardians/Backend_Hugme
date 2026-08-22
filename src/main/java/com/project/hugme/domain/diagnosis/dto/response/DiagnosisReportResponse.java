package com.project.hugme.domain.diagnosis.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;

/**
 * 진단 리포트 공개 응답.
 * AI 분석 결과는 그대로 펼쳐 내려주고, 등기 권리관계 요약을 함께 담는다.
 */
public record DiagnosisReportResponse(
        @JsonUnwrapped FastApiDiagnosisResponse diagnosis,
        RegistrySummaryResponse registry,
        RegistryVerificationResponse registryVerification
) {

    public static DiagnosisReportResponse of(
            FastApiDiagnosisResponse diagnosis,
            RegistrySummaryResponse registry,
            RegistryVerificationResponse registryVerification
    ) {
        return new DiagnosisReportResponse(diagnosis, registry, registryVerification);
    }
}
