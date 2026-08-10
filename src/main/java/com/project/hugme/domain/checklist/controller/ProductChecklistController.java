package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.ItemDocumentsResponse;
import com.project.hugme.domain.checklist.dto.ProductChecklistResponse;
import com.project.hugme.domain.checklist.service.ProductChecklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductChecklistController {

    private final ProductChecklistService productChecklistService;

    @GetMapping("/{productCode}/checklist")
    public ResponseEntity<ProductChecklistResponse> getProductChecklist(
            @PathVariable String productCode
    ) {

        ProductChecklistResponse response = productChecklistService.getProductChecklist(productCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productCode}/checklist/items/{itemId}/documents")
    public ResponseEntity<ItemDocumentsResponse> getItemDocuments(
            @PathVariable String productCode,
            @PathVariable Long itemId
    ) {

        ItemDocumentsResponse response = productChecklistService.getItemDocuments(productCode, itemId);
        return ResponseEntity.ok(response);

    }
}
