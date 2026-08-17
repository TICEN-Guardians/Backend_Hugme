package com.project.hugme.domain.chatbot.document.dto;

import jakarta.validation.constraints.NotNull;

public record DocumentPreparationUpdateRequest(@NotNull Boolean prepared) {
}
