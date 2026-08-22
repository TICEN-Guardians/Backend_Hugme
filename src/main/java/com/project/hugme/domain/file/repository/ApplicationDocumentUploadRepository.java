package com.project.hugme.domain.file.repository;

import com.project.hugme.domain.file.entity.ApplicationDocumentUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationDocumentUploadRepository
        extends JpaRepository<ApplicationDocumentUpload, Long> {

    Optional<ApplicationDocumentUpload> findByUploadIdAndApplicationUserUserId(
            Long uploadId,
            Long userId
    );

    List<ApplicationDocumentUpload> findAllByApplicationApplicationIdAndApplicationUserUserIdOrderByCreatedAtDesc(
            Long applicationId,
            Long userId
    );

    boolean existsByApplicationApplicationIdAndDocumentDocumentId(
            Long applicationId,
            Long documentId
    );
}
