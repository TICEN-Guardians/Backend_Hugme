package com.project.hugme.domain.chatbot.guide.dto;

import java.util.List;

public record ChatResponse(
        String sessionId,
        String answer,
        String category,
        List<SourceDto> sources,
        List<String> suggestedQuestions,
        RedirectDto redirect
) {}