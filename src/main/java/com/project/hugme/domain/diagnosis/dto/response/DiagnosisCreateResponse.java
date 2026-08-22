package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
import com.project.hugme.domain.diagnosis.enums.DiagnosisStatus;

import java.time.Instant;

public record DiagnosisCreateResponse(
        Long analysisId,
        DiagnosisStatus status,
        DiagnosisMode mode,
        String accessToken,
        Instant accessTokenExpiresAt
) {
    public static DiagnosisCreateResponse from(
            Diagnosis diagnosis,
            String accessToken
    ) {
        return new DiagnosisCreateResponse(
                diagnosis.getAnalysisId(),
                diagnosis.getStatus(),
                diagnosis.getMode(),
                accessToken,
                diagnosis.getAnonymousAccessExpiresAt()
        );
    }
}
