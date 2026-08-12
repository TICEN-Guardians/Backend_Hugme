package com.project.hugme.domain.checklist.entity.application;

import com.project.hugme.domain.checklist.entity.product.Document;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "checklist_document_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistDocumentResult {

    @EmbeddedId
    private ChecklistDocumentResultId id;

    @MapsId("applicationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    @MapsId("documentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private Document document;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    private ChecklistDocumentResult(
            Application application,
            Document document
    ) {
        this.id = new ChecklistDocumentResultId(
                application.getApplicationId(),
                document.getDocumentId()
        );
        this.application = application;
        this.document = document;
    }

    public static ChecklistDocumentResult create(
            Application application,
            Document document
    ) {
        return new ChecklistDocumentResult(
                application,
                document
        );
    }
}