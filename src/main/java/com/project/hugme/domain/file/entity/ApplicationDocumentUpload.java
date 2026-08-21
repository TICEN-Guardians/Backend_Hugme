package com.project.hugme.domain.file.entity;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.product.Document;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "application_document_uploads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDocumentUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Long uploadId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_id",
            nullable = false
    )
    private Document document;

    @Column(
            name = "user_file_name",
            nullable = false,
            length = 500
    )
    private String userFileName;

    @Column(
            name = "storage_key",
            nullable = false,
            length = 1000
    )
    private String storageKey;

    @Column(
            name = "mime_type",
            nullable = false,
            length = 100
    )
    private String mimeType;

    @Column(
            name = "file_size",
            nullable = false
    )
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 30)
    private DocumentValidationStatus validationStatus;

    @Column(name = "detected_document_type", length = 100)
    private String detectedDocumentType;

    @Column(name = "validation_confidence")
    private Double validationConfidence;

    @Column(name = "validation_message", length = 1000)
    private String validationMessage;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    private ApplicationDocumentUpload(
            Application application,
            Document document,
            String userFileName,
            String storageKey,
            String mimeType,
            Long fileSize
    ) {
        this.application = application;
        this.document = document;
        this.userFileName = userFileName;
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.validationStatus = DocumentValidationStatus.PROCESSING;
    }

    public static ApplicationDocumentUpload create(
            Application application,
            Document document,
            String userFileName,
            String storageKey,
            String mimeType,
            Long fileSize
    ) {
        return new ApplicationDocumentUpload(
                application,
                document,
                userFileName,
                storageKey,
                mimeType,
                fileSize
        );
    }

    public void completeValidation(
            DocumentValidationStatus status,
            String detectedDocumentType,
            Double confidence,
            String message
    ) {
        this.validationStatus = status;
        this.detectedDocumentType = detectedDocumentType;
        this.validationConfidence = confidence;
        this.validationMessage = message;
        this.validatedAt = Instant.now();
    }
}
