package com.project.hugme.infra.ai.intent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DocumentIntentFieldMapper {

    private DocumentIntentFieldMapper() {
    }

    public static List<String> getResponseFields(
            DocumentQuestionIntent intent
    ) {

        return switch (intent) {

            case DOCUMENT_SEARCH -> List.of();

            case DOCUMENT_PURPOSE -> List.of(
                    "description",
                    "notes"
            );

            case ISSUE_METHOD -> List.of(
                    "preparation_method",
                    "online_availability",
                    "online_url",
                    "offline_availability",
                    "offline_location",
                    "issuer",
                    "contact_info",
                    "official_guide_url"
            );

            case ONLINE_ISSUANCE -> List.of(
                    "online_availability",
                    "online_url",
                    "preparation_method"
            );

            case OFFLINE_ISSUANCE -> List.of(
                    "offline_availability",
                    "offline_location",
                    "preparation_method",
                    "required_documents"
            );

            case REQUIREMENTS -> List.of(
                    "required_documents"
            );

            case APPLICANT_ELIGIBILITY -> List.of(
                    "applicant_eligibility"
            );

            case FEE -> List.of(
                    "fee"
            );

            case PROCESSING_TIME -> List.of(
                    "processing_time"
            );

            case PRECAUTIONS -> List.of(
                    "notes",
                    "required_documents",
                    "applicant_eligibility"
            );

            case OFFICIAL_ISSUE_SITE -> List.of(
                    "online_url",
                    "official_guide_url",
                    "offline_location"
            );

            case OTHER -> List.of();
        };
    }

    public static List<String> getResponseFields(
            List<DocumentQuestionIntent> intents
    ) {

        Set<String> fields = new LinkedHashSet<>();

        for (DocumentQuestionIntent intent : intents) {
            fields.addAll(getResponseFields(intent));
        }

        return List.copyOf(fields);
    }

    public static List<String> getDocumentSearchFields() {

        return List.of(
                "document_name",
                "document_group_name",
                "description",
                "content"
        );
    }

    public static String getEmbeddingField() {
        return "embedding";
    }
}
