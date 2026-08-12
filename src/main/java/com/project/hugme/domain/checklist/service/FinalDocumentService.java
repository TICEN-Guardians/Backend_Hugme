package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResult;
import com.project.hugme.domain.checklist.entity.application.PartyType;
import com.project.hugme.domain.checklist.entity.product.ChecklistItem;
import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.entity.product.Product;
import com.project.hugme.domain.checklist.entity.question.QuestionOption;
import com.project.hugme.domain.checklist.repository.ChecklistDocumentResultRepository;
import com.project.hugme.domain.checklist.repository.QuestionOptionRepository;
import com.project.hugme.domain.checklist.repository.product.ChecklistItemRepository;
import com.project.hugme.domain.checklist.repository.product.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinalDocumentService {

    /*
     * OCR 조건으로 활성화되는 준비 항목명
     */
    private static final String TENANT_COMPANY_ITEM =
            "임차인이 법인인 경우";

    private static final String LANDLORD_COMPANY_ITEM =
            "임대인이 법인인 경우";

    private static final String LANDLORD_PROXY_ITEM =
            "임대인의 대리인과 계약한 경우";

    /*
     * OCR 결과에 따라 생략할 수 있는 서류명
     */
    private static final String LEASE_REPORT_DOCUMENT =
            "주택 임대차 계약 신고필증";

    private static final String OFFICETEL_CONFIRMATION_DOCUMENT =
            "중개대상물 확인·설명서";

    private final ChecklistItemRepository checklistItemRepository;
    private final DocumentRepository documentRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ChecklistDocumentResultRepository resultRepository;

    /*
     * 최종 서류를 다시 계산하여 저장한다.
     */
    @Transactional
    public void rebuild(
            ApplicationInfo applicationInfo,
            List<QuestionOption> selectedOptions
    ) {
        Product product =
                applicationInfo
                        .getApplication()
                        .getProduct();

        HousingType housingType =
                applicationInfo.getHousingType();

        if (housingType == null) {
            throw new IllegalStateException(
                    "확정된 주택유형이 없습니다."
            );
        }

        /*
         * 현재 상품에 연결된 전체 준비 항목
         */
        List<ChecklistItem> productItems =
                checklistItemRepository
                        .findDistinctByProducts_ProductCode(
                                product.getProductCode()
                        );

        /*
         * 기본·OCR·질문 조건으로 활성화된 itemId
         */
        Set<Long> activeItemIds =
                resolveActiveItemIds(
                        applicationInfo,
                        productItems,
                        selectedOptions
                );

        /*
         * 활성화된 항목으로 최종 서류 계산
         */
        Map<Long, Document> finalDocuments =
                resolveFinalDocuments(
                        applicationInfo,
                        activeItemIds,
                        selectedOptions
                );

        /*
         * 기존 결과를 새로운 결과로 교체
         */
        replaceResults(
                applicationInfo.getApplication(),
                finalDocuments
        );
    }

    /*
     * 기본 항목, OCR 항목, 질문 항목을 하나로 모은다.
     */
    private Set<Long> resolveActiveItemIds(
            ApplicationInfo applicationInfo,
            List<ChecklistItem> productItems,
            List<QuestionOption> selectedOptions
    ) {
        HousingType housingType =
                applicationInfo.getHousingType();

        Set<Long> activeItemIds =
                new LinkedHashSet<>();

        Set<Long> productItemIds =
                new HashSet<>();

        /*
         * 기본 포함 항목
         */
        for (ChecklistItem productItem : productItems) {

            productItemIds.add(
                    productItem.getItemId()
            );

            if (!productItem.isDefaultIncluded()) {
                continue;
            }

            if (!matchesHousingType(
                    productItem.getHousingTypes(),
                    housingType
            )) {
                continue;
            }

            activeItemIds.add(
                    productItem.getItemId()
            );
        }

        /*
         * OCR: 임차인이 법인
         */
        if (applicationInfo.getTenantType()
                == PartyType.COMPANY) {

            addItemByName(
                    TENANT_COMPANY_ITEM,
                    productItems,
                    housingType,
                    activeItemIds
            );
        }

        /*
         * OCR: 임대인이 법인
         */
        if (applicationInfo.getLandlordType()
                == PartyType.COMPANY) {

            addItemByName(
                    LANDLORD_COMPANY_ITEM,
                    productItems,
                    housingType,
                    activeItemIds
            );
        }

        /*
         * OCR: 임대인의 대리인과 계약
         */
        if (Boolean.TRUE.equals(
                applicationInfo.getLandlordProxyContract()
        )) {
            addItemByName(
                    LANDLORD_PROXY_ITEM,
                    productItems,
                    housingType,
                    activeItemIds
            );
        }

        /*
         * 질문 선택지에 연결된 준비 항목
         */
        for (QuestionOption selectedOption : selectedOptions) {

            for (ChecklistItem affectedItem
                    : selectedOption.getAffectedItems()) {

                Long affectedItemId =
                        affectedItem.getItemId();

                /*
                 * 현재 상품에 연결되지 않은 항목 제외
                 */
                if (!productItemIds.contains(
                        affectedItemId
                )) {
                    continue;
                }

                if (!matchesHousingType(
                        affectedItem.getHousingTypes(),
                        housingType
                )) {
                    continue;
                }

                activeItemIds.add(affectedItemId);
            }
        }

        return activeItemIds;
    }

    /*
     * 상품 준비 항목 중 이름이 일치하는 항목을 활성화한다.
     */
    private void addItemByName(
            String itemName,
            List<ChecklistItem> productItems,
            HousingType housingType,
            Set<Long> activeItemIds
    ) {
        for (ChecklistItem productItem : productItems) {

            if (!itemName.equals(
                    productItem.getItemName()
            )) {
                continue;
            }

            if (!matchesHousingType(
                    productItem.getHousingTypes(),
                    housingType
            )) {
                continue;
            }

            activeItemIds.add(
                    productItem.getItemId()
            );
        }
    }

    /*
     * 활성화된 항목에 속하는 최종 서류를 계산한다.
     */
    private Map<Long, Document> resolveFinalDocuments(
            ApplicationInfo applicationInfo,
            Set<Long> activeItemIds,
            List<QuestionOption> selectedOptions
    ) {
        Map<Long, Document> finalDocuments =
                new LinkedHashMap<>();

        if (activeItemIds.isEmpty()) {
            return finalDocuments;
        }

        List<Long> activeItemIdList =
                new ArrayList<>(activeItemIds);

        /*
         * 활성화된 항목의 모든 서류
         */
        List<Document> itemDocuments =
                documentRepository
                        .findAllWithDocumentGroup(
                                activeItemIdList
                        );

        /*
         * 질문 선택지를 통해서만 표시되는 서류 ID
         */
        List<Long> conditionalDocumentIdList =
                questionOptionRepository
                        .findAllConditionalDocumentIds();

        Set<Long> conditionalDocumentIds =
                new HashSet<>(
                        conditionalDocumentIdList
                );

        /*
         * 기본 서류 반영
         */
        for (Document document : itemDocuments) {

            /*
             * 질문 선택지 전용 서류는 기본 결과에서 제외
             */
            if (conditionalDocumentIds.contains(
                    document.getDocumentId()
            )) {
                continue;
            }

            if (!isDocumentAvailable(
                    document,
                    applicationInfo
            )) {
                continue;
            }

            finalDocuments.put(
                    document.getDocumentId(),
                    document
            );
        }

        /*
         * 선택지에 직접 연결된 실제 서류 반영
         */
        for (QuestionOption selectedOption : selectedOptions) {

            for (Document affectedDocument
                    : selectedOption.getAffectedDocuments()) {

                Long affectedItemId =
                        affectedDocument
                                .getItem()
                                .getItemId();

                /*
                 * 해당 서류가 속한 준비 항목도 활성화되어야 한다.
                 */
                if (!activeItemIds.contains(
                        affectedItemId
                )) {
                    continue;
                }

                if (!isDocumentAvailable(
                        affectedDocument,
                        applicationInfo
                )) {
                    continue;
                }

                /*
                 * documentId 기준으로 중복 제거
                 */
                finalDocuments.put(
                        affectedDocument.getDocumentId(),
                        affectedDocument
                );
            }
        }

        return finalDocuments;
    }

    /*
     * 서류의 주택유형과 OCR 조건을 함께 확인한다.
     */
    private boolean isDocumentAvailable(
            Document document,
            ApplicationInfo applicationInfo
    ) {
        boolean housingTypeMatched =
                matchesHousingType(
                        document.getHousingTypes(),
                        applicationInfo.getHousingType()
                );

        if (!housingTypeMatched) {
            return false;
        }

        String documentName =
                document.getDocumentName();

        /*
         * 계약서에서 확정일자를 확인할 수 있으면
         * 신고필증을 생략한다.
         */
        if (Boolean.TRUE.equals(
                applicationInfo.getFixedDateConfirmed()
        ) && LEASE_REPORT_DOCUMENT.equals(
                documentName
        )) {
            return false;
        }

        /*
         * 오피스텔 주거용 표시가 있으면
         * 중개대상물 확인·설명서를 생략한다.
         */
        if (Boolean.TRUE.equals(
                applicationInfo
                        .getOfficetelResidentialMarked()
        ) && OFFICETEL_CONFIRMATION_DOCUMENT.equals(
                documentName
        )) {
            return false;
        }

        return true;
    }

    /*
     * 주택유형 매핑을 확인한다.
     */
    private boolean matchesHousingType(
            Set<HousingType> allowedHousingTypes,
            HousingType applicationHousingType
    ) {
        /*
         * 매핑이 없으면 모든 주택유형에 적용
         */
        if (allowedHousingTypes.isEmpty()) {
            return true;
        }

        for (HousingType allowedHousingType
                : allowedHousingTypes) {

            if (allowedHousingType.getHousingTypeCode()
                    == applicationHousingType
                    .getHousingTypeCode()) {

                return true;
            }
        }

        return false;
    }

    /*
     * 기존 결과를 삭제하고 새로운 결과를 저장한다.
     */
    private void replaceResults(
            Application application,
            Map<Long, Document> finalDocuments
    ) {
        resultRepository.deleteAllByApplicationId(
                application.getApplicationId()
        );

        List<ChecklistDocumentResult> results =
                new ArrayList<>();

        for (Document document
                : finalDocuments.values()) {

            ChecklistDocumentResult result =
                    ChecklistDocumentResult.create(
                            application,
                            document
                    );

            results.add(result);
        }

        resultRepository.saveAll(results);


    }
}