package com.project.hugme.domain.chatbot.document.service;

import com.project.hugme.infra.ai.embedding.BgeM3EmbeddingService;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {

    private final DocumentContentBuilder contentBuilder;
    private final BgeM3EmbeddingService embeddingService;

    public float[] createEmbedding(DocumentSearchData data) throws Exception {

        String content = contentBuilder.build(data);

        return embeddingService.embed(content);
    }
}
