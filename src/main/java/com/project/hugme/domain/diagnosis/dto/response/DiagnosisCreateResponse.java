package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.enums.DiagnosisStatus;

public record DiagnosisCreateResponse(
        Long analysisId,
        DiagnosisStatus status
) {

    public static DiagnosisCreateResponse from(Diagnosis diagnosis) {
        return new DiagnosisCreateResponse(
                diagnosis.getAnalysisId(),
                diagnosis.getStatus()
        );
    }
}