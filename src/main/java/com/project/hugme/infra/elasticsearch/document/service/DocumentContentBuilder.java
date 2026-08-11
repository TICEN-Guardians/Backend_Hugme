package com.project.hugme.infra.elasticsearch.document.service;

import com.project.hugme.infra.elasticsearch.document.dto.DocumentSearchData;
import org.springframework.stereotype.Component;

@Component
public class DocumentContentBuilder {

    public String build(DocumentSearchData data) {

        StringBuilder content = new StringBuilder();

        append(content, "서류명", data.documentName());
        append(content, "서류 그룹", data.documentGroupName());
        append(content, "설명", data.description());
        append(content, "발급 기관", data.issuer());

        append(content, "발급 및 준비 방법", data.preparationMethod());

        append(content, "온라인 발급 가능 여부", data.onlineAvailability());
        append(content, "온라인 발급 URL", data.onlineUrl());

        append(content, "오프라인 발급 가능 여부", data.offlineAvailability());
        append(content, "오프라인 발급 위치", data.offlineLocation());

        append(content, "발급 신청 시 구비서류", data.requiredDocuments());
        append(content, "신청 자격", data.applicantEligibility());

        append(content, "수수료", data.fee());
        append(content, "처리 기간", data.processingTime());

        if (data.notes() != null && !data.notes().isEmpty()) {
            append(content, "유의사항", String.join(" ", data.notes()));
        }

        append(content, "담당 기관", data.contactInfo());
        append(content, "공식 안내 URL", data.officialGuideUrl());

        return content.toString().trim();
    }

    private void append(
            StringBuilder builder,
            String label,
            String value
    ) {

        if (value == null || value.isBlank()) {
            return;
        }

        builder.append(label)
                .append(": ")
                .append(value)
                .append("\n");
    }
}