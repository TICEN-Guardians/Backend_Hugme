package com.project.hugme.domain.chatbot.document.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.project.hugme.domain.chatbot.document.dto.DocumentIndexDocument;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchData;
import com.project.hugme.domain.chatbot.document.repository.DocumentSearchDataRepository;
import com.project.hugme.infra.ai.embedding.BgeM3EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexingService {

    private final ElasticsearchClient elasticsearchClient;
    private final DocumentSearchDataRepository repository;
    private final DocumentContentBuilder contentBuilder;
    private final BgeM3EmbeddingService embeddingService;

    @Value("${hugme.elasticsearch.document-index}")
    private String indexName;

    public void indexAll() throws Exception {

        List<DocumentSearchData> documents = repository.findAll();

        log.info("서류 Elasticsearch 색인 시작: {}건", documents.size());

        for (DocumentSearchData data : documents) {
            index(data);
        }

        log.info("서류 Elasticsearch 색인 완료: {}건", documents.size());
    }

    public void index(DocumentSearchData data) throws Exception {

        String content = contentBuilder.build(data);

        float[] embedding = embeddingService.embed(content);

        DocumentIndexDocument document =
                new DocumentIndexDocument(
                        data.documentId(),
                        data.documentName(),
                        data.documentGroupName(),
                        data.description(),
                        data.issuer(),
                        content,
                        data.preparationMethod(),
                        data.onlineAvailability(),
                        data.onlineUrl(),
                        data.offlineAvailability(),
                        data.offlineLocation(),
                        data.requiredDocuments(),
                        data.applicantEligibility(),
                        data.fee(),
                        data.processingTime(),
                        data.notes(),
                        data.contactInfo(),
                        data.officialGuideUrl(),
                        data.hugReferenceUrls(),
                        data.verifiedAt(),
                        embedding
                );

        elasticsearchClient.index(i -> i
                .index(indexName)
                .id(String.valueOf(data.documentId()))
                .document(document)
        );
    }
}