package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.application.MyDocumentListResponse;
import com.project.hugme.domain.checklist.dto.application.MyDocumentResponse;
import com.project.hugme.domain.checklist.dto.application.MyDocumentSectionResponse;
import com.project.hugme.domain.checklist.dto.product.DocumentGroupResponse;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResult;
import com.project.hugme.domain.checklist.entity.product.ChecklistSection;
import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.entity.product.DocumentGroup;
import com.project.hugme.domain.checklist.entity.product.SectionCode;
import com.project.hugme.domain.checklist.repository.ApplicationInfoRepository;
import com.project.hugme.domain.checklist.repository.ChecklistDocumentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyDocumentService {

    private final ApplicationInfoRepository applicationInfoRepository;
    private final ChecklistDocumentResultRepository resultRepository;

    public MyDocumentListResponse getCurrentDocuments(
            Long userId,
            Long applicationId
    ) {
        /*
         * applicationId가 로그인 사용자의 신청인지 확인한다.
         */
        ApplicationInfo applicationInfo =
                applicationInfoRepository
                        .findByApplicationIdAndUserId(
                                applicationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "신청정보를 찾을 수 없습니다."
                                )
                        );

        /*
         * checklist_document_results에 저장된
         * 사용자별 최종 서류를 조회한다.
         */
        List<ChecklistDocumentResult> results =
                resultRepository.findCurrentDocuments(
                        applicationId,
                        userId
                );

        List<MyDocumentSectionResponse> sectionResponses =
                new ArrayList<>();

        /*
         * BASIC → ADDITIONAL → DISCOUNT 순서로 처리한다.
         */
        for (SectionCode sectionCode
                : SectionCode.values()) {

            String sectionName = null;

            List<MyDocumentResponse> documentResponses =
                    new ArrayList<>();

            for (ChecklistDocumentResult result : results) {

                Document document =
                        result.getDocument();

                ChecklistSection section =
                        document
                                .getItem()
                                .getSection();

                /*
                 * 현재 처리 중인 Section이 아니면 제외한다.
                 */
                if (section.getSectionCode()
                        != sectionCode) {
                    continue;
                }

                sectionName =
                        section.getSectionName();

                /*
                 * 택1 서류 그룹이 있으면 함께 반환한다.
                 */
                DocumentGroupResponse documentGroupResponse =
                        null;

                DocumentGroup documentGroup =
                        document.getDocumentGroup();

                if (documentGroup != null) {
                    documentGroupResponse =
                            new DocumentGroupResponse(
                                    documentGroup
                                            .getDocumentGroupId(),
                                    documentGroup.getGroupName(),
                                    documentGroup.getSortOrder()
                            );
                }

                MyDocumentResponse documentResponse =
                        new MyDocumentResponse(
                                document.getDocumentId(),
                                document.getDocumentName(),
                                document.getDescription(),
                                document.getSampleImageUrl(),
                                document.getSortOrder(),
                                documentGroupResponse
                        );

                documentResponses.add(
                        documentResponse
                );
            }

            /*
             * 최종 서류가 존재하는 Section만 응답한다.
             */
            if (!documentResponses.isEmpty()) {

                MyDocumentSectionResponse sectionResponse =
                        new MyDocumentSectionResponse(
                                sectionCode,
                                sectionName,
                                documentResponses
                        );

                sectionResponses.add(
                        sectionResponse
                );
            }
        }

        return new MyDocumentListResponse(
                applicationInfo.getApplicationId(),
                sectionResponses
        );
    }
}