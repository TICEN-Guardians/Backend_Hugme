package com.project.hugme.domain.chatbot.document.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.project.hugme.infra.ai.embedding.BgeM3EmbeddingService;
import com.project.hugme.domain.chatbot.document.dto.DocumentIndexDocument;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchResponse;
import com.project.hugme.infra.ai.intent.DocumentBm25FieldMapper;
import com.project.hugme.infra.ai.intent.DocumentIntentFieldMapper;
import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final BgeM3EmbeddingService embeddingService;

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

    public List<DocumentSearchResponse> searchByBm25(
            String query,
            List<DocumentQuestionIntent> intents,
            int topk
    ) throws Exception {
        List<String> searchFields =
                DocumentBm25FieldMapper.getSearchFields(intents);

        if (searchFields.isEmpty()) {
            return List.of();
        }

        long bm25StartNanos = System.nanoTime();
        var response = elasticsearchClient.search(
                s -> s
                        .index(indexName)
                        .size(topk)
                        .query(q -> q
                                .multiMatch(m -> m
                                        .query(query)
                                        .fields(searchFields)
                                )
                        ),
                DocumentIndexDocument.class
        );
        log.info("[PERF][document-rag] stage=bm25 duration_ms={}",
                elapsedMillis(bm25StartNanos));

        List<String> responseFields =
                DocumentIntentFieldMapper.getResponseFields(intents);

        return response.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> {
                    DocumentIndexDocument document = hit.source();

                    Map<String, Object> fields =
                            extractFields(document, responseFields);

                    return new DocumentSearchResponse(
                            document.documentId(),
                            document.documentName(),
                            fields,
                            document.officialGuideUrl(),
                            document.hugReferenceUrls(),
                            hit.score()
                    );
                })
                .toList();
    }

    public List<DocumentSearchResponse> searchByVector(
            String query,
            List<DocumentQuestionIntent> intents,
            int topk
    ) throws Exception {

        long embeddingStartNanos = System.nanoTime();
        float[] queryEmbedding =
                embeddingService.embed(query);
        log.info("[PERF][document-rag] stage=embedding duration_ms={}",
                elapsedMillis(embeddingStartNanos));

        List<Float> queryVector =
                toFloatList(queryEmbedding);

        int numCandidates = 20;

        long vectorStartNanos = System.nanoTime();
        var response = elasticsearchClient.search(
                s -> s
                        .index(indexName)
                        .size(topk)
                        .knn(k -> k
                                .field("embedding")
                                .queryVector(queryVector)
                                .k(topk)
                                .numCandidates(numCandidates)
                        ),
                DocumentIndexDocument.class
        );
        log.info("[PERF][document-rag] stage=vector duration_ms={}",
                elapsedMillis(vectorStartNanos));

        List<String> responseFields =
                DocumentIntentFieldMapper.getResponseFields(intents);

        return response.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> {

                    DocumentIndexDocument document = hit.source();

                    Map<String, Object> fields =
                            extractFields(document, responseFields);

                    return new DocumentSearchResponse(
                            document.documentId(),
                            document.documentName(),
                            fields,
                            document.officialGuideUrl(),
                            document.hugReferenceUrls(),
                            hit.score()
                    );
                })
                .toList();
    }

    public List<DocumentSearchResponse> searchByOnlineAvailability(
            String availability,
            List<DocumentQuestionIntent> intents,
            int limit
    ) throws Exception {
        var response = elasticsearchClient.search(
                s -> s
                        .index(indexName)
                        .size(limit)
                        .query(q -> q
                                .term(t -> t
                                        .field("online_availability")
                                        .value(availability)
                                )
                        ),
                DocumentIndexDocument.class
        );

        List<String> responseFields =
                DocumentIntentFieldMapper.getResponseFields(intents);

        return response.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> toSearchResponse(hit.source(), responseFields, hit.score()))
                .toList();
    }

    private List<Float> toFloatList(float[] vector) {

        List<Float> result =
                new ArrayList<>(vector.length);

        for (float value : vector) {
            result.add(value);
        }

        return result;
    }

    private DocumentSearchResponse toSearchResponse(
            DocumentIndexDocument document,
            List<String> responseFields,
            Double score
    ) {
        Map<String, Object> fields = extractFields(document, responseFields);

        return new DocumentSearchResponse(
                document.documentId(),
                document.documentName(),
                fields,
                document.officialGuideUrl(),
                document.hugReferenceUrls(),
                score
        );
    }

    public List<DocumentSearchResponse> searchByHybrid(
            String query,
            List<DocumentQuestionIntent> intents,
            int topK
    ) throws Exception {

        // RRF에 사용할 후보를 최종 Top-K보다 넉넉하게 가져옴
        int candidateK = Math.max(20, topK * 5);

        List<DocumentSearchResponse> bm25Results =
                searchByBm25(query, intents, candidateK);

        List<DocumentSearchResponse> vectorResults =
                searchByVector(query, intents, candidateK);

        return mergeByRrf(
                bm25Results,
                vectorResults,
                topK
        );
    }

    private List<DocumentSearchResponse> mergeByRrf(
            List<DocumentSearchResponse> bm25Results,
            List<DocumentSearchResponse> vectorResults,
            int topK
    ) {

        long rrfStartNanos = System.nanoTime();

        final int RRF_K = 60;

        Map<Long, Double> rrfScores = new HashMap<>();
        Map<Long, DocumentSearchResponse> documents = new LinkedHashMap<>();

        // BM25 순위 반영
        for (int i = 0; i < bm25Results.size(); i++) {

            DocumentSearchResponse result = bm25Results.get(i);

            int rank = i + 1;

            double score =
                    1.0 / (RRF_K + rank);

            rrfScores.merge(
                    result.documentId(),
                    score,
                    Double::sum
            );

            documents.putIfAbsent(
                    result.documentId(),
                    result
            );
        }

        for (int i = 0; i < vectorResults.size(); i++) {

            DocumentSearchResponse result = vectorResults.get(i);

            int rank = i + 1;

            double score =
                    1.0 / (RRF_K + rank);

            rrfScores.merge(
                    result.documentId(),
                    score,
                    Double::sum
            );

            documents.putIfAbsent(
                    result.documentId(),
                    result
            );
        }

        List<DocumentSearchResponse> mergedResults = rrfScores.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<Long, Double>comparingByValue()
                                .reversed()
                )
                .limit(topK)
                .map(entry -> {

                    DocumentSearchResponse original =
                            documents.get(entry.getKey());

                    return new DocumentSearchResponse(
                            original.documentId(),
                            original.documentName(),
                            original.fields(),
                            original.officialGuideUrl(),
                            original.hugReferenceUrls(),
                            entry.getValue()
                    );
                })
                .toList();
        log.info("[PERF][document-rag] stage=rrf duration_ms={}",
                elapsedMillis(rrfStartNanos));
        return mergedResults;
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

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
