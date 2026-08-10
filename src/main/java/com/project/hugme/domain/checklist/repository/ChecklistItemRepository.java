package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.ChecklistItem;
import com.project.hugme.domain.checklist.entity.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findDistinctByProducts_ProductCode(ProductCode productCode);

}
