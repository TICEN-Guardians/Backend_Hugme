package com.project.hugme.domain.chatbot.document.service;

import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchResponse;
import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;
import com.project.hugme.infra.ai.intent.DocumentStructuredQuery;
import com.project.hugme.infra.ai.intent.DocumentStructuredQueryParser;
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
