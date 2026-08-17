package com.project.hugme.domain.chatbot.document.entity;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.product.Document;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "document_preparation_checks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_document_preparation_check_application_document",
                columnNames = {"application_id", "document_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentPreparationCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @CreationTimestamp
    @Column(name = "checked_at", nullable = false, updatable = false)
    private Instant checkedAt;

    private DocumentPreparationCheck(Application application, Document document) {
        this.application = application;
        this.document = document;
    }

    public static DocumentPreparationCheck create(
            Application application,
            Document document
    ) {
        return new DocumentPreparationCheck(application, document);
    }
}
