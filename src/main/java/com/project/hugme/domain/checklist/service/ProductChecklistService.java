package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.dto.product.ChecklistItemResponse;
import com.project.hugme.domain.checklist.dto.product.ProductChecklistResponse;
import com.project.hugme.domain.checklist.entity.product.*;
import com.project.hugme.domain.checklist.repository.ChecklistItemRepository;
import com.project.hugme.domain.checklist.repository.DocumentRepository;
import com.project.hugme.domain.checklist.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductChecklistService {

    private final ProductRepository productRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final DocumentRepository documentRepository;

    public ProductChecklistResponse getProductChecklist(ProductCode productCode) {
        /*
         * 1단계
         * productCode로 상품을 조회한다.
         *
         *
         * 조회 결과:
         * - productId
         * - productCode
         * - productName
         */
        Product product = productRepository
                .findByProductCode(productCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다: " + productCode
                        )
                );

        /*
         * 2단계
         * checklist_item_products 테이블을 통해
         * 해당 상품에 연결된 checklist_items를 조회한다.
         *

         */
        List<ChecklistItem> checklistItems =
                checklistItemRepository
                        .findAllByProductCodeWithSectionAndGroup(
                                productCode
                        );


        /*
         * 3단계
         * 조회한 checklistItem의 itemId 목록을 만든다.
         */
        List<Long> itemIds = new ArrayList<>();
        for (ChecklistItem checklistItem : checklistItems) {
            Long itemId = checklistItem.getItemId();
            itemIds.add(itemId);
        }

        /*
         * 4단계
         * itemId 목록을 사용해 documents를 한 번에 조회한다.
         *
         * 이때 documentGroup도 함께 조회한다.
         *
         */

        List<Document> documents;

        if (itemIds.isEmpty()) {
            documents = new ArrayList<>();
        } else {

            documents =
                    documentRepository
                            .findAllWithDocumentGroup(itemIds);
        }


        /*
         * 5단계
         * document를 itemId 기준으로 묶는다.
         *
         * 예:
         * itemId 1 -> 전세계약서 관련 서류 목록
         * itemId 2 -> 전입세대확인서 관련 서류 목록
         */

        Map<Long, List<Document>> documentsByItemId = new LinkedHashMap<>();

        for (Document document : documents) {
            Long itemId = document.getItem().getItemId();

            /*
             * 해당 itemId가 Map에 처음 등장한 경우
             * 문서를 담을 빈 목록을 먼저 만든다.
             */
            List<Document> itemDocuments;

            /*
             * 이미 해당 itemId의 목록이 있으면
             * 기존 목록을 가져온다.
             */
            if (documentsByItemId.containsKey(itemId)) {
                itemDocuments =
                        documentsByItemId.get(itemId);
            } else {
                /*
                 * 처음 등장한 itemId라면
                 * 새로운 빈 목록을 만든다.
                 */
                itemDocuments =
                        new ArrayList<>();
            }
            /*
             * 현재 문서를 목록에 추가한다.
             */
            itemDocuments.add(document);

            /*
             * 문서가 들어 있는 목록을 Map에 저장한다.
             */
            documentsByItemId.put(
                    itemId,
                    itemDocuments
            );
        }


        List<ChecklistItemResponse> checklistItemResponses = new ArrayList<>();

        for (ChecklistItem checklistItem : checklistItems) {
            ChecklistSection section =
                    checklistItem.getSection();

            ChecklistGroup group =
                    checklistItem.getGroup();

            //기본일 경우 group이 null 일 수 있음
            Long groupId = null;
            String groupName = null;
            Integer groupSortOrder = null;

            if (group != null) {
                groupId = group.getGroupId();
                groupName = group.getGroupName();
                groupSortOrder = group.getSortOrder();
            }
            /*
             * 현재 item의 실제 서류
             */
            List<Document> itemDocuments =
                    documentsByItemId.get(
                            checklistItem.getItemId()
                    );

            if (itemDocuments == null) {
                itemDocuments = new ArrayList<>();
            }

            List<DocumentResponse> documentResponses =
                    new ArrayList<>();

            //documentResponse 형태 만들기
            for (Document document : itemDocuments) {

                DocumentResponse documentResponse =
                        DocumentResponse.from(document);

                documentResponses.add(documentResponse);
            }


            /*
             * section과 group 정보를 item 응답에 직접 넣는다.
             */
            ChecklistItemResponse itemResponse =
                    new ChecklistItemResponse(
                            checklistItem.getItemId(),
                            checklistItem.getItemName(),
                            checklistItem.getSortOrder(),
                            checklistItem.isDefaultIncluded(),

                            section.getSectionCode(),
                            section.getSectionName(),

                            groupId,
                            groupName,
                            groupSortOrder,

                            documentResponses
                    );

            checklistItemResponses.add(itemResponse);
        }
        /*
         * 7단계
         * 완성된 ProductChecklistResponse를 반환한다.
         */

        return new ProductChecklistResponse(
                product.getProductCode(),
                product.getProductName(),
                checklistItemResponses
        );
    }

}