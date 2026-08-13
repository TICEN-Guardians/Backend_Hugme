package com.project.hugme.infra.ai.intent.dto;

import com.project.hugme.infra.ai.intent.DocumentQuestionIntent;

import java.util.List;

public record DocumentStructuredQuery(
        String documentName, // 서류명
        List<DocumentQuestionIntent> intents, // 서류에 대한 의도 목록
        String normalizedQuestion // 정규화된 질문
) {
}