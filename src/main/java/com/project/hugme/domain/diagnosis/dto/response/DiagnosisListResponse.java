package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record DiagnosisListResponse(
        Long analysisId,
        String normalizedAddress,
        LocalDate updatedDate
) {
    public static DiagnosisListResponse from(
            Diagnosis diagnosis
    ) {
        Instant updatedAt =
                diagnosis.getUpdatedAt();

        LocalDate updatedDate =
                updatedAt
                        .atZone(
                                ZoneId.of("Asia/Seoul")
                        )
                        .toLocalDate();

        return new DiagnosisListResponse(
                diagnosis.getAnalysisId(),
                diagnosis.getNormalizedAddress(),
                updatedDate
        );
    }
}