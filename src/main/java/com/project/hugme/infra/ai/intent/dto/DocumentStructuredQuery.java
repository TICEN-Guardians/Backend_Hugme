package com.project.hugme.infra.ai.intent.dto;

import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;

import java.util.List;

public record DocumentStructuredQuery(
        String documentName,
        List<DocumentQuestionIntent> intents,
        String normalizedQuestion
) {
}