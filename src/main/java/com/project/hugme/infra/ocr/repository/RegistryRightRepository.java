package com.project.hugme.infra.ocr.repository;

import com.project.hugme.infra.ocr.entity.RegistryRight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistryRightRepository
        extends JpaRepository<RegistryRight, Long> {

    List<RegistryRight> findByRegistryResultRegistryResultIdOrderByRightIdAsc(
            Long registryResultId
    );
}
