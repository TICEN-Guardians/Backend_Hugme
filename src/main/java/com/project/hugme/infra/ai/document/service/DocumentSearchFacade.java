package com.project.hugme.infra.ai.document.service;

import com.project.hugme.infra.ai.document.dto.DocumentSearchRequest;
import com.project.hugme.infra.ai.document.dto.DocumentSearchResponse;
import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;
import com.project.hugme.infra.ai.intent.dto.DocumentStructuredQuery;
import com.project.hugme.infra.ai.intent.service.DocumentStructuredQueryParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentSearchFacade {

    private final DocumentStructuredQueryParser structuredQueryParser;
    private final DocumentSearchService documentSearchService;

    public List<DocumentSearchResponse> search(
            DocumentSearchRequest request
    ) throws Exception {
        DocumentStructuredQuery structuredQuery =
                structuredQueryParser.parse(request.question());

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
    }
}
