package com.project.hugme.domain.file.dto;

import com.project.hugme.domain.file.entity.ApplicationDocumentUpload;

import java.time.Instant;

public record FileUploadResponse(
        Long uploadId,
        Long applicationId,
        Long documentId,
        String userFileName,
        String mimeType,
        Long fileSize,
        Instant createdAt
) {

    public static FileUploadResponse from(
            ApplicationDocumentUpload upload
    ) {
        return new FileUploadResponse(
                upload.getUploadId(),
                upload.getApplication().getApplicationId(),
                upload.getDocument().getDocumentId(),
                upload.getUserFileName(),
                upload.getMimeType(),
                upload.getFileSize(),
                upload.getCreatedAt()
        );
    }
}