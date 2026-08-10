package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.ItemDocumentsResponse;
import com.project.hugme.domain.checklist.dto.ProductChecklistResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductChecklistService {


    public ProductChecklistResponse getProductChecklist(String productCode) {
    }

    public ItemDocumentsResponse getItemDocuments(String productCode, Long itemId) {

        return ItemDocumentsResponse
    }
}
