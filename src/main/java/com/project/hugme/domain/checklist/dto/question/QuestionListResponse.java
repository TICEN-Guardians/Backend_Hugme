package com.project.hugme.domain.checklist.dto.question;

import com.project.hugme.domain.checklist.entity.question.Question;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;

import java.util.ArrayList;
import java.util.List;

public record QuestionListResponse(
        QuestionStep questionStep,
        List<QuestionResponse> questions

) {
    public static QuestionListResponse from(
            QuestionStep questionStep,
            List<Question> questions
    ) {
        List<QuestionResponse> questionResponses = new ArrayList<>();

        for (Question question : questions) {

            QuestionResponse questionResponse = QuestionResponse.from(question);

            questionResponses.add(questionResponse);
        }


        return new QuestionListResponse(
                questionStep,
                questionResponses
        );
    }


}


