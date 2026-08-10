package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
                SELECT document
                FROM Document document
                LEFT JOIN FETCH document.documentGroup
                WHERE document.item.itemId IN :itemIds
            """)
    List<Document> findAllWithDocumentGroup(
            @Param("itemIds") List<Long> itemIds
    );

   
}
