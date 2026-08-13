package com.project.hugme.domain.checklist.dto.question;

import com.project.hugme.domain.checklist.entity.question.QuestionOption;

public record QuestionOptionResponse(
        Long optionId,
        String optionText

) {
    public static QuestionOptionResponse from(
            QuestionOption questionOption
    ) {
        return new QuestionOptionResponse(
                questionOption.getOptionId(),
                questionOption.getOptionText()
        );
    }

}
