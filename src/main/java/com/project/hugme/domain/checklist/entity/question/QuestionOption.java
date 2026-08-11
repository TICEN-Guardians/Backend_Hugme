package com.project.hugme.domain.checklist.entity.question;

import com.project.hugme.domain.checklist.entity.product.ChecklistItem;
import com.project.hugme.domain.checklist.entity.product.Document;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "question_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "question_id",
            nullable = false
    )
    private Question question;

    @Column(
            name = "option_text",
            nullable = false,
            length = 100
    )
    private String optionText;

    // 이 선택지를 선택하면 추가되는 준비 항목
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "question_item_effects",
            joinColumns = @JoinColumn(name = "option_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private Set<ChecklistItem> affectedItems = new HashSet<>();

    //이 선택지를 선택하면 추가되는 서류
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_document_effects",
            joinColumns = @JoinColumn(name = "option_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    private Set<Document> affectedDocuments = new HashSet<>();

    //이 선택지를 선택하면 다음에 표시될 하위 질문
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_dependencies",
            joinColumns = @JoinColumn(name = "parent_option_id"),
            inverseJoinColumns = @JoinColumn(name = "child_question_id")
    )
    private Set<Question> childQuestions = new HashSet<>();
}