package com.project.hugme.infra.elasticsearch.document.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.project.hugme.infra.elasticsearch.document.dto.DocumentIndexDocument;
import com.project.hugme.infra.elasticsearch.document.dto.DocumentSearchResponse;
import com.project.hugme.infra.elasticsearch.intent.DocumentIntentFieldMapper;
import com.project.hugme.infra.elasticsearch.intent.DocumentQuestionIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${hugme.elasticsearch.document-index}")
    private String indexName;

    public DocumentSearchResponse searchByDocumentId(
            Long documentId,
            List<DocumentQuestionIntent> intents
    ) throws Exception {
        GetResponse<DocumentIndexDocument> response =
                elasticsearchClient.get(
                        g -> g
                                .index(indexName)
                                .id(String.valueOf(documentId)),
                        DocumentIndexDocument.class
                );
        if (!response.found() || response.source() == null) {
            throw new IllegalArgumentException(
                    "해당 서류를 찾을 수 없습니다. documentId = " + documentId
            );
        }

        DocumentIndexDocument document = response.source();

        List<String> responseFields =
                DocumentIntentFieldMapper.getResponseFields(intents);

        Map<String, Object> fields =
                extractFields(document, responseFields);

        return new DocumentSearchResponse(
                document.documentId(),
                document.documentName(),
                fields,
                document.officialGuideUrl(),
                document.hugReferenceUrls(),
                null
        );
    }

    private Map<String, Object> extractFields(
            DocumentIndexDocument document,
            List<String> responseFields
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();

        for (String field : responseFields) {

            Object value = switch (field) {

                case "description" -> document.description();
                case "notes" -> document.notes();
                case "preparation_method" -> document.preparationMethod();
                case "online_availability" -> document.onlineAvailability();
                case "online_url" -> document.onlineUrl();
                case "offline_availability" -> document.offlineAvailability();
                case "offline_location" -> document.offlineLocation();
                case "issuer" -> document.issuer();
                case "contact_info" -> document.contactInfo();
                case "official_guide_url" -> document.officialGuideUrl();
                case "required_documents" -> document.requiredDocuments();
                case "applicant_eligibility" -> document.applicantEligibility();
                case "fee" -> document.fee();
                case "processing_time" -> document.processingTime();
                default -> null;
            };
            if (value != null) {
                fields.put(field, value);
            }
        }

        return fields;
    }
}
