package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.question.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, Long> {

    @Query("""
                SELECT DISTINCT questionOption
                FROM QuestionOption questionOption
                JOIN FETCH questionOption.question
                LEFT JOIN FETCH questionOption.childQuestions
                WHERE questionOption.optionId IN :selectedOptionIds
            """)
    List<QuestionOption> findAllWithChildQuestions(
            @Param("selectedOptionIds")
            List<Long> selectedOptionIds
    );

    @Query("""
                SELECT DISTINCT document.documentId
                FROM QuestionOption questionOption
                JOIN questionOption.affectedDocuments document
            """)
    List<Long> findAllConditionalDocumentIds();
}