package com.project.hugme.domain.checklist.repository.product;

import com.project.hugme.domain.checklist.entity.product.ChecklistItem;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.product.SectionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {


    List<ChecklistItem> findDistinctByProducts_ProductCode(ProductCode productCode);

    @Query("""
            SELECT DISTINCT item
            FROM ChecklistItem item
            JOIN item.products product
            JOIN FETCH item.section section
            LEFT JOIN FETCH item.group checklistGroup
            WHERE product.productCode = :productCode
              AND section.sectionCode = :sectionCode
            ORDER BY
                checklistGroup.sortOrder,
                item.sortOrder,
                item.itemId
            """)
    List<ChecklistItem> findAllByProductCodeAndSectionCode(
            @Param("productCode")
            ProductCode productCode,
            @Param("sectionCode")
            SectionCode sectionCode
    );

    @Query("""
            SELECT DISTINCT item
            FROM ChecklistItem item
            JOIN item.products product
            JOIN FETCH item.section section
            LEFT JOIN FETCH item.group checklistGroup
            WHERE product.productCode = :productCode
              AND item.itemId = :itemId
            """)
    Optional<ChecklistItem> findByProductCodeAndItemId(
            @Param("productCode")
            ProductCode productCode,

            @Param("itemId")
            Long itemId
    );

}
