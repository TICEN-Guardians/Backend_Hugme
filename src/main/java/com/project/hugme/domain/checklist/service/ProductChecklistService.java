package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.*;
import com.project.hugme.domain.checklist.dto.product.ProductChecklistResponse;
import com.project.hugme.domain.checklist.entity.*;
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
         * 이때 함께 필요한 정보:
         * - checklistSection
         * - checklistGroup
         */
        List<ChecklistItem> checklistItems = checklistItemRepository.findDistinctByProducts_ProductCode(productCode);


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


        /*
         * 6단계
         * Entity를 응답 DTO로 변환한다.
         *
         * 변환 구조:
         * ProductChecklistResponse
         * └─ ChecklistSectionResponse
         *    └─ ChecklistItemResponse
         *       └─ DocumentResponse
         *          └─ DocumentGroupResponse
         */
        List<ChecklistSectionResponse> sectionResponses = new ArrayList<>();

        /*
         * 현재 sectionCode에 속한 Item을 찾는다.
         */

        for (SectionCode sectionCode : SectionCode.values()) {
            ChecklistSection currentSection = null;
            List<ChecklistItemResponse> sectionItemResponses = new ArrayList<>();

            /*
             * 현재 처리 중인 section이 아니면 건너뛴다.
             */
            for (ChecklistItem checklistItem : checklistItems) {
                ChecklistSection itemSection = checklistItem.getSection();

                if (itemSection.getSectionCode() != sectionCode) {
                    continue;
                }
                currentSection = itemSection;
                /*
                 * 5단계에서 itemId별로 묶은 Document를 가져온다.
                 */
                List<Document> itemDocuments = documentsByItemId.get(checklistItem.getItemId());

                if (itemDocuments == null || itemDocuments.isEmpty()) {
                    throw new IllegalStateException(
                            "준비 항목에 연결된 서류가 없습니다. itemId="
                                    + checklistItem.getItemId()
                    );
                }
                /*
                 * Document Entity를 DocumentResponse로 변환한다.
                 */
                List<DocumentResponse> documentResponses = new ArrayList<>();

                for (Document document : itemDocuments) {
                    DocumentGroupResponse documentGroupResponse = null;

                    DocumentGroup documentGroup = document.getDocumentGroup();

                    if (documentGroup != null) {
                        documentGroupResponse = new DocumentGroupResponse(
                                documentGroup.getDocumentGroupId(),
                                documentGroup.getGroupName(),
                                documentGroup.getSortOrder()
                        );
                    }
                    DocumentResponse documentResponse =
                            new DocumentResponse(
                                    document.getDocumentId(),
                                    document.getDocumentName(),
                                    document.getDescription(),
                                    document.getSampleImageUrl(),
                                    document.getSortOrder(),
                                    documentGroupResponse
                            );
                    documentResponses.add(documentResponse);
                }

                ChecklistGroupResponse checklistGroupResponse = null;

                ChecklistGroup checklistGroup = checklistItem.getGroup();

                if (checklistGroup != null) {
                    checklistGroupResponse = new ChecklistGroupResponse(
                            checklistGroup.getGroupId(),
                            checklistGroup.getGroupName(),
                            checklistGroup.getSortOrder()

                    );
                }
                /* 마지막 만들기
                 */
                ChecklistItemResponse itemResponse = new ChecklistItemResponse(
                        checklistItem.getItemId(),
                        checklistItem.getItemName(),
                        checklistItem.getSortOrder(),
                        checklistItem.isDefaultIncluded(),
                        checklistGroupResponse,
                        documentResponses


                );

                sectionItemResponses.add(itemResponse);


            }
            if (currentSection != null) {
                ChecklistSectionResponse sectionResponse =
                        new ChecklistSectionResponse(
                                currentSection.getSectionId(),
                                currentSection.getSectionCode(),
                                currentSection.getSectionName(),
                                sectionItemResponses
                        );
                sectionResponses.add(sectionResponse);

            }

        }


        /*
         * 7단계
         * 완성된 ProductChecklistResponse를 반환한다.
         */

        return new ProductChecklistResponse(
                product.getProductCode(),
                product.getProductName(),
                sectionResponses
        );
    }

}



