package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResult;
import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistDocumentResultRepository extends JpaRepository<
        ChecklistDocumentResult,
        ChecklistDocumentResultId
        > {

    @Modifying
    @Query("""
                DELETE FROM ChecklistDocumentResult result
                WHERE result.application.applicationId = :applicationId
            """)
    void deleteAllByApplicationId(
            @Param("applicationId") Long applicationId
    );

    @Query("""
                SELECT result
                FROM ChecklistDocumentResult result
                JOIN FETCH result.document document
                JOIN FETCH document.item item
                JOIN FETCH item.section section
                LEFT JOIN FETCH document.documentGroup documentGroup
                WHERE result.application.applicationId = :applicationId
                  AND result.application.user.userId = :userId
                ORDER BY
                    section.sectionId,
                    item.sortOrder,
                    documentGroup.sortOrder,
                    document.sortOrder
            """)
    List<ChecklistDocumentResult> findCurrentDocuments(
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId
    );
}