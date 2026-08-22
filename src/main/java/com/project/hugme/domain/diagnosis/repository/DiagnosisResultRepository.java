package com.project.hugme.domain.diagnosis.repository;

import com.project.hugme.domain.diagnosis.entity.DiagnosisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisResultRepository
        extends JpaRepository<DiagnosisResult, Long> {

    Optional<DiagnosisResult> findByDiagnosisAnalysisId(Long analysisId);
}
