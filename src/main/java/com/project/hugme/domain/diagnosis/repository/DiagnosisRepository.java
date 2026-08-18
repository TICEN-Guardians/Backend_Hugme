package com.project.hugme.domain.diagnosis.repository;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    Optional<Diagnosis> findByAnalysisIdAndUserUserId(
            Long analysisId,
            Long userId
    );
}
