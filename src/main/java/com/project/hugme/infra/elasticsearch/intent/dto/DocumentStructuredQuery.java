package com.project.hugme.infra.elasticsearch.intent.dto;

import com.project.hugme.infra.elasticsearch.intent.DocumentQuestionIntent;

import java.util.List;

public record DocumentStructuredQuery(
        String documentName,
        List<DocumentQuestionIntent> intents,
        String normalizedQuestion
) {
}