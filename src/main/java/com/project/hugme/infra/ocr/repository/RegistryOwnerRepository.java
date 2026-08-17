package com.project.hugme.infra.ocr.repository;

import com.project.hugme.infra.ocr.entity.RegistryOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistryOwnerRepository
        extends JpaRepository<RegistryOwner, Long> {

    List<RegistryOwner> findByRegistryResultRegistryResultIdOrderByRegistryOwnerIdAsc(
            Long registryResultId
    );
}
