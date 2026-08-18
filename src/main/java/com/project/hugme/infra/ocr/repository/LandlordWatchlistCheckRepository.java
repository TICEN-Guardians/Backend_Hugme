package com.project.hugme.infra.ocr.repository;

import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LandlordWatchlistCheckRepository
        extends JpaRepository<LandlordWatchlistCheck, Long> {

    List<LandlordWatchlistCheck>
    findByRegistryResultRegistryResultIdOrderByCheckIdAsc(
            Long registryResultId
    );
}
