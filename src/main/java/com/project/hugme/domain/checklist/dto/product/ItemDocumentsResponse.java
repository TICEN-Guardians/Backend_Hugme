package com.project.hugme.domain.checklist.dto.product;

import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.entity.product.ProductCode;

import java.util.List;

public record ItemDocumentsResponse(
        ProductCode productCode,
        Long itemId,
        String itemName,
        List<DocumentResponse> documents
) {
}