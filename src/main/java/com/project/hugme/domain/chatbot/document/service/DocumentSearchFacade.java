package com.project.hugme.domain.chatbot.document.service;

import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchResponse;
import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;
import com.project.hugme.infra.ai.intent.DocumentStructuredQuery;
import com.project.hugme.infra.ai.intent.DocumentStructuredQueryParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentSearchFacade {

    private final DocumentStructuredQueryParser structuredQueryParser;
    private final DocumentSearchService documentSearchService;

    public List<DocumentSearchResponse> search(
            DocumentSearchRequest request
    ) throws Exception {
        long intentStartNanos = System.nanoTime();
        DocumentStructuredQuery structuredQuery =
                structuredQueryParser.parse(request.question());
        log.info("[PERF][document-rag] stage=intent duration_ms={}",
                elapsedMillis(intentStartNanos));

        long searchStartNanos = System.nanoTime();
        try {
            List<DocumentQuestionIntent> intents =
                    structuredQuery.intents();

            if (intents.contains(DocumentQuestionIntent.OTHER)) {
                return List.of();
            }

            if (request.documentId() != null) {
                DocumentSearchResponse response =
                        documentSearchService.searchByDocumentId(
                                request.documentId(),
                                intents
                        );

                return List.of(response);
            }
            return documentSearchService.searchByHybrid(
                    structuredQuery.normalizedQuestion(),
                    intents,
                    3
            );
        } finally {
            log.info("[PERF][document-rag] stage=search_total duration_ms={}",
                    elapsedMillis(searchStartNanos));
        }
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
