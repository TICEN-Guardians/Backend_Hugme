package com.project.hugme.domain.checklist.dto.question;

import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import jakarta.validation.constraints.NotEmpty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record QuestionAnswersRequest(
        @NotNull
        QuestionStep currentStep,

        boolean finalSubmission,

        @NotEmpty
        List<Long> selectedOptionIds
) {
}
