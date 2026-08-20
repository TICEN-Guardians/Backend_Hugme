package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.application.*;
import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ApplicationStatus;
import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResult;
import com.project.hugme.domain.checklist.entity.product.*;
import com.project.hugme.domain.checklist.repository.ApplicationInfoRepository;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.checklist.repository.ChecklistDocumentResultRepository;
import com.project.hugme.domain.checklist.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDocumentService {

    private final ApplicationInfoRepository applicationInfoRepository;
    private final ApplicationRepository applicationRepository;
    private final ChecklistDocumentResultRepository resultRepository;
    private final ProductRepository productRepository;


    public ResultDocumentListResponse getResultDocuments(
            Long userId,
            Long applicationId
    ) {
        Application application =
                applicationRepository.findByApplicationIdAndUser_UserId(applicationId, userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("신청 정보를 찾을 수 없습니다.")
                        );


        //질문 결과로 확정된 서류만 조회
        List<ChecklistDocumentResult> results
                = resultRepository.findCurrentDocuments(applicationId, userId);



        //ResultDocumentListResponse - applicationId, <ResultDocumentSectionResponse>
        //ResultDocumentSectionResponse - sectionCode, sectionName, <ResultDocumentItemResponse>
        //ResultDocumentItemResponse - item, group , <DocumentResponse>=results

        List<ResultDocumentSectionResponse> sectionResponses = new ArrayList<>();
        for(SectionCode sectionCode : SectionCode.values()){

            List<ResultDocumentItemResponse> items =  new ArrayList<>();

            String sectionName = null;

            Map<Long, ChecklistItem> itemById = new LinkedHashMap<>();
            Map<Long, List<DocumentResponse>> documentsByItemId = new LinkedHashMap<>();


            for(ChecklistDocumentResult result : results){
                Document document= result.getDocument();
                ChecklistItem item = document.getItem();
                ChecklistSection section = item.getSection();

                if(section.getSectionCode()!=sectionCode){
                    continue;
                }
                sectionName = section.getSectionName();
                Long itemId = item.getItemId();

                if (!itemById.containsKey(itemId)) {
                    itemById.put(itemId, item);
                }

                if(!documentsByItemId.containsKey(itemId)){
                    documentsByItemId.put(itemId,new ArrayList<>());
                }

                List<DocumentResponse> documents = documentsByItemId.get(itemId);
                documents.add(DocumentResponse.from(document));


            }
            for (Long itemId : itemById.keySet()) {

                ChecklistItem item = itemById.get(itemId);

                List<DocumentResponse> documents = documentsByItemId.get(itemId);

                ChecklistGroup group = item.getGroup();

                Long groupId = null;
                String groupName = null;
                Integer groupSortOrder = null;

                if (group != null) {
                    groupId = group.getGroupId();
                    groupName = group.getGroupName();
                    groupSortOrder = group.getSortOrder();
                }

                items.add(
                        new ResultDocumentItemResponse(
                                item.getItemId(),
                                item.getItemName(),
                                item.getSortOrder(),
                                item.isDefaultIncluded(),
                                groupId,
                                groupName,
                                groupSortOrder,
                                documents
                        )
                );
            }
            if (!items.isEmpty()) {
                sectionResponses.add(
                        new ResultDocumentSectionResponse(
                                sectionCode,
                                sectionName,
                                items
                        )
                );
            }
        }

        return new ResultDocumentListResponse(
                applicationId,
                sectionResponses
        );
    }

    public MyDocumentListResponse getCurrentDocuments(
            Long userId,
            Long applicationId
    ) {
        ApplicationInfo applicationInfo =
                applicationInfoRepository.findByApplicationIdAndUserId(applicationId, userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "신청정보를 찾을 수 없습니다."
                                )
                        );

        List<ChecklistDocumentResult> results = resultRepository.findCurrentDocuments(applicationId, userId);

        List<MyDocumentSectionResponse> sectionResponses = new ArrayList<>();

        /*
         * BASIC → ADDITIONAL → DISCOUNT 순서
         */
        for (SectionCode sectionCode : SectionCode.values()) {

            String sectionName = null;

            /*
             * 현재 section에 속하는 서류를 담을 목록
             */
            List<DocumentResponse> documentResponses = new ArrayList<>();

            /*
             * 최종 서류를 한 번만 순회
             */
            for (ChecklistDocumentResult result : results) {

                Document document = result.getDocument();

                ChecklistSection section = document
                        .getItem()
                        .getSection();

                /*
                 * 현재 section이 아니면 제외
                 */
                if (section.getSectionCode() != sectionCode) {
                    continue;
                }
                sectionName =
                        section.getSectionName();

                documentResponses.add(
                        DocumentResponse.from(document)
                );
            }
            /*
             * 서류가 존재하는 section만 응답에 추가
             */
            if (!documentResponses.isEmpty()) {

                MyDocumentSectionResponse sectionResponse =
                        new MyDocumentSectionResponse(
                                sectionCode,
                                sectionName,
                                documentResponses
                        );

                sectionResponses.add(sectionResponse);
            }
        }

        return new MyDocumentListResponse(
                applicationInfo.getApplicationId(),
                sectionResponses
        );
    }
    @Transactional(readOnly = true)
    public boolean hasResultDocuments(
            Long userId, ProductCode productCode
    ) {
        // productCode가 없으면 기존 로직 사용
        if (productCode == null) {
            return hasResultDocuments(userId);
        }

        Optional<Application> existingApplication =
                applicationRepository.findByUser_UserId(userId);

        if (existingApplication.isEmpty()) {
            return false;
        }

        Application application =
                existingApplication.get();

        boolean completed =
                application.getApplicationStatus()
                        == ApplicationStatus.DONE;

        ProductCode existingProductCode =
                application
                        .getProduct()
                        .getProductCode();

        boolean sameProduct =
                existingProductCode == productCode;

        boolean documentsExist =
                hasResultDocuments(userId);

        return completed
                && sameProduct
                && documentsExist;
    }

    public boolean hasResultDocuments(Long userId) {
        Application application = applicationRepository.findByUser_UserId(userId)
                .orElseThrow(()->new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));

        boolean result=false;
        if(application.getApplicationStatus()== ApplicationStatus.DONE){
            result=true;
        }
        return result;

    }

}