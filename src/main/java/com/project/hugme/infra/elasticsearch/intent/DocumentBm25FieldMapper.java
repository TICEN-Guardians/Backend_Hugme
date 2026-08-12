package com.project.hugme.infra.elasticsearch.intent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DocumentBm25FieldMapper {

    private DocumentBm25FieldMapper() {
    }

    public static List<String> getSearchFields(
            DocumentQuestionIntent intent
    ) {
        return switch (intent) {

            case DOCUMENT_SEARCH -> List.of(
                    "document_name^4",
                    "description^3",
                    "document_group_name^2",
                    "content"

            );
            case DOCUMENT_PURPOSE -> List.of(
                    "document_name^4",
                    "description^3",
                    "notes^2",
                    "content"
            );

            case ISSUE_METHOD -> List.of(
                    "document_name^4",
                    "preparation_method^3",
                    "offline_location^2",
                    "issuer^2",
                    "contact_info^2",
                    "content"
            );

            case ONLINE_ISSUANCE -> List.of(
                    "document_name^4",
                    "preparation_method^3",
                    "content"
            );

            case OFFLINE_ISSUANCE -> List.of(
                    "document_name^4",
                    "offline_location^3",
                    "preparation_method^3",
                    "required_documents^2",
                    "content"
            );

            case REQUIREMENTS -> List.of(
                    "document_name^4",
                    "required_documents^3",
                    "content"
            );

            case APPLICANT_ELIGIBILITY -> List.of(
                    "document_name^4",
                    "applicant_eligibility^3",
                    "content"
            );

            case FEE -> List.of(
                    "document_name^4",
                    "fee^3",
                    "content"
            );

            case PROCESSING_TIME -> List.of(
                    "document_name^4",
                    "processing_time^3",
                    "content"
            );

            case PRECAUTIONS -> List.of(
                    "document_name^4",
                    "notes^3",
                    "required_documents^2",
                    "applicant_eligibility^2",
                    "content"
            );

            case OFFICIAL_ISSUE_SITE -> List.of(
                    "document_name^4",
                    "issuer^3",
                    "contact_info^2",
                    "offline_location^2",
                    "content"
            );
            case OTHER -> List.of();
        };
    }

    public static List<String> getSearchFields(
            List<DocumentQuestionIntent> intents
    ) {
        Set<String> fields = new LinkedHashSet<>();

        for (DocumentQuestionIntent intent : intents) {
            fields.addAll(getSearchFields(intent));
        }

        return List.copyOf(fields);
    }
}
