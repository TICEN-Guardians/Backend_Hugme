package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.product.ChecklistItem;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    @Query("""
            SELECT DISTINCT item
            FROM ChecklistItem item
            JOIN item.products product
            JOIN FETCH item.section section
            LEFT JOIN FETCH item.group checklistGroup
            WHERE product.productCode = :productCode
            ORDER BY
                section.sectionId,
                checklistGroup.sortOrder,
                item.sortOrder
            """)
    List<ChecklistItem> findAllByProductCodeWithSectionAndGroup(
            @Param("productCode") ProductCode productCode
    );

    List<ChecklistItem> findDistinctByProducts_ProductCode(ProductCode productCode);

}
