package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.dto.product.ChecklistItemResponse;
import com.project.hugme.domain.checklist.dto.product.ItemDocumentsResponse;
import com.project.hugme.domain.checklist.dto.product.ProductChecklistResponse;
import com.project.hugme.domain.checklist.entity.product.*;
import com.project.hugme.domain.checklist.repository.ProductRepository;
import com.project.hugme.domain.checklist.repository.product.ChecklistItemRepository;
import com.project.hugme.domain.checklist.repository.product.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductChecklistService {

    private final ProductRepository productRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final DocumentRepository documentRepository;

    public ProductChecklistResponse getProductChecklist(
            ProductCode productCode,
            SectionCode sectionCode
    ) {
        Product product =
                productRepository
                        .findByProductCode(productCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 상품입니다: "
                                                + productCode
                                )
                        );

        List<ChecklistItem> checklistItems =
                checklistItemRepository
                        .findAllByProductCodeAndSectionCode(
                                productCode,
                                sectionCode
                        );

        List<ChecklistItemResponse> itemResponses =
                new ArrayList<>();

        String sectionName = null;

        for (ChecklistItem checklistItem : checklistItems) {

            ChecklistSection section =
                    checklistItem.getSection();

            sectionName = section.getSectionName();

            ChecklistGroup group =
                    checklistItem.getGroup();

            //기본서류는 group = null
            Long groupId = null;
            String groupName = null;
            Integer groupSortOrder = null;

            if (group != null) {
                groupId = group.getGroupId();
                groupName = group.getGroupName();
                groupSortOrder = group.getSortOrder();
            }

            ChecklistItemResponse itemResponse =
                    new ChecklistItemResponse(
                            checklistItem.getItemId(),
                            checklistItem.getItemName(),
                            checklistItem.getSortOrder(),
                            checklistItem.isDefaultIncluded(),
                            groupId,
                            groupName,
                            groupSortOrder
                    );

            itemResponses.add(itemResponse);
        }

        return new ProductChecklistResponse(
                product.getProductCode(),
                product.getProductName(),
                sectionCode,
                sectionName,
                itemResponses
        );
    }

    public ItemDocumentsResponse getItemDocuments(
            ProductCode productCode,
            Long itemId
    ) {
        /*
         * 해당 상품에 실제로 연결된 Item인지 확인한다.
         */
        ChecklistItem checklistItem =
                checklistItemRepository
                        .findByProductCodeAndItemId(
                                productCode,
                                itemId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 상품에 포함되지 않은 준비 항목입니다."
                                )
                        );

        List<Document> documents =
                documentRepository
                        .findAllByItemIdWithDocumentGroup(
                                itemId
                        );

        List<DocumentResponse> documentResponses =
                new ArrayList<>();

        for (Document document : documents) {
            documentResponses.add(
                    DocumentResponse.from(document)
            );
        }

        return new ItemDocumentsResponse(
                productCode,
                checklistItem.getItemId(),
                checklistItem.getItemName(),
                documentResponses
        );
    }

}