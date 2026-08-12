package com.project.hugme.domain.checklist.dto.question;

import com.project.hugme.domain.checklist.entity.question.Question;
import com.project.hugme.domain.checklist.entity.question.QuestionOption;

import java.util.ArrayList;
import java.util.List;

public record QuestionResponse(
        Long questionId,
        String questionText,
        List<QuestionOptionResponse> options

) {

    public static QuestionResponse from(
            Question question

    ) {
        List<QuestionOptionResponse> optionResponses = new ArrayList<>();

        for (QuestionOption questionOption : question.getOptions()) {

            QuestionOptionResponse optionResponse = QuestionOptionResponse.from(questionOption);

            optionResponses.add(optionResponse);

        }

        return new QuestionResponse(
                question.getQuestionId(),
                question.getQuestionText(),
                optionResponses
        );


    }
}
