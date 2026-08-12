package com.project.hugme.domain.checklist.dto.question;

import com.project.hugme.domain.checklist.entity.question.QuestionStep;

import java.util.List;

public record QuestionAnswersResponse(
        QuestionStep completedStep,
        QuestionStep nextStep,
        boolean questionnaireCompleted,
        List<QuestionResponse> additionalQuestions

) {
}
