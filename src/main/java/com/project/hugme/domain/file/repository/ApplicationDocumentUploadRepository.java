package com.project.hugme.domain.file.repository;

import com.project.hugme.domain.file.entity.ApplicationDocumentUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationDocumentUploadRepository
        extends JpaRepository<ApplicationDocumentUpload, Long> {
}