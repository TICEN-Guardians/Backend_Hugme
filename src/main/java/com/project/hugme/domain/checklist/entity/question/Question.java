package com.project.hugme.domain.checklist.entity.question;

import com.project.hugme.domain.checklist.entity.application.PartyType;
import com.project.hugme.domain.checklist.entity.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "questions")
@NoArgsConstructor
@Getter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_step",
            nullable = false,
            length = 20)
    private QuestionStep questionStep;

    @Column(
            name = "question_order",
            nullable = false
    )
    private Integer questionOrder;

    @Column(
            name = "question_text",
            nullable = false,
            length = 500
    )
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", length = 20)
    private PartyType tenantType;

    @Enumerated(EnumType.STRING)
    @Column(name = "landlord_type", length = 20)
    private PartyType landlordType;

    @OneToMany(
            mappedBy = "question",
            fetch = FetchType.LAZY
    )
    @OrderBy("optionId ASC")
    private List<QuestionOption> options = new ArrayList<>();

    /*
     * question_products 연결 테이블
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_products",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();
}
