package com.project.hugme.domain.chatbot.document.repository;

import com.project.hugme.domain.chatbot.document.entity.DocumentPreparationCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface DocumentPreparationCheckRepository extends JpaRepository<DocumentPreparationCheck, Long> {
    @Query("""
            SELECT check.document.documentId
            FROM DocumentPreparationCheck check
            WHERE check.application.applicationId = :applicationId
            """)
    Set<Long> findCheckedDocumentIds(@Param("applicationId") Long applicationId);

    void deleteByApplicationApplicationIdAndDocumentDocumentId(Long applicationId, Long documentId);
}
