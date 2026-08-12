package com.project.hugme.domain.checklist.service;

import com.project.hugme.domain.checklist.dto.application.MyDocumentListResponse;
import com.project.hugme.domain.checklist.dto.application.MyDocumentSectionResponse;
import com.project.hugme.domain.checklist.dto.common.DocumentResponse;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ChecklistDocumentResult;
import com.project.hugme.domain.checklist.entity.product.ChecklistSection;
import com.project.hugme.domain.checklist.entity.product.Document;
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
}