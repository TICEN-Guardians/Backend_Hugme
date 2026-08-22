package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.question.Question;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
                SELECT DISTINCT question
                FROM Question question
                JOIN question.products product
                LEFT JOIN FETCH question.options
                WHERE product.productCode = :productCode
                  AND question.questionStep = :questionStep
                ORDER BY question.questionOrder
            """)
    List<Question> findQuestions(
            @Param("productCode") ProductCode productCode,
            @Param("questionStep") QuestionStep questionStep
    );
}
