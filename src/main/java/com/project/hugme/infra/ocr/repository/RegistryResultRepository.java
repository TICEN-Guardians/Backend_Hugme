package com.project.hugme.infra.ocr.repository;

import com.project.hugme.infra.ocr.entity.RegistryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistryResultRepository
        extends JpaRepository<RegistryResult, Long> {

    Optional<RegistryResult> findTopByAnalysisIdOrderByParsedAtDesc(
            Long analysisId
    );
}
