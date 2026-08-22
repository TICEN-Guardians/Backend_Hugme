package com.project.hugme.domain.file.service;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.checklist.repository.ChecklistDocumentResultRepository;
import com.project.hugme.domain.checklist.repository.product.DocumentRepository;
import com.project.hugme.domain.file.dto.FileAccessUrlResponse;
import com.project.hugme.domain.file.dto.FileUploadResponse;
import com.project.hugme.domain.file.dto.StoredFile;
import com.project.hugme.domain.file.entity.ApplicationDocumentUpload;
import com.project.hugme.domain.file.repository.ApplicationDocumentUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationDocumentUploadService {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ChecklistDocumentResultRepository checklistDocumentResultRepository;
    private final ApplicationDocumentUploadRepository uploadRepository;
    private final S3FileStorageService s3FileStorageService;
    private final UploadFileValidator uploadFileValidator;
    private final DocumentGuardrailService documentGuardrailService;

    @Transactional
    public FileUploadResponse upload(
            Long userId,
            Long applicationId,
            Long documentId,
            MultipartFile file
    ) {
        Application application = applicationRepository
                .findByApplicationIdAndUser_UserId(applicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("신청정보를 찾을 수 없습니다."));

        if (!checklistDocumentResultRepository
                .existsByApplicationApplicationIdAndApplicationUserUserIdAndDocumentDocumentId(
                        applicationId, userId, documentId)) {
            throw new IllegalArgumentException("해당 신청에 포함된 준비서류가 아닙니다.");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("서류정보를 찾을 수 없습니다."));
        String mimeType = uploadFileValidator.validateAndDetectMimeType(file);
        StoredFile storedFile = s3FileStorageService.store(
                applicationId, documentId, file, mimeType);

        try {
            ApplicationDocumentUpload upload = ApplicationDocumentUpload.create(
                    application,
                    document,
                    storedFile.originalFileName(),
                    storedFile.storageKey(),
                    storedFile.mimeType(),
                    storedFile.fileSize()
            );
            uploadRepository.save(upload);

            DocumentGuardrailResult result = documentGuardrailService.validate(
                    document.getDocumentName(), file, mimeType);
            upload.completeValidation(
                    result.status(),
                    result.detectedDocumentType(),
                    result.confidence(),
                    result.message()
            );
            return FileUploadResponse.from(upload);
        } catch (RuntimeException exception) {
            s3FileStorageService.delete(storedFile.storageKey());
            throw exception;
        }
    }

    public FileUploadResponse get(Long userId, Long uploadId) {
        return FileUploadResponse.from(getOwnedUpload(userId, uploadId));
    }

    public List<FileUploadResponse> list(Long userId, Long applicationId) {
        applicationRepository.findByApplicationIdAndUser_UserId(applicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("신청정보를 찾을 수 없습니다."));
        return uploadRepository
                .findAllByApplicationApplicationIdAndApplicationUserUserIdOrderByCreatedAtDesc(
                        applicationId, userId)
                .stream()
                .map(FileUploadResponse::from)
                .toList();
    }

    public FileAccessUrlResponse preview(Long userId, Long uploadId) {
        ApplicationDocumentUpload upload = getOwnedUpload(userId, uploadId);
        return s3FileStorageService.createAccessUrl(
                upload.getStorageKey(), upload.getUserFileName(), upload.getMimeType(), true);
    }

    public FileAccessUrlResponse download(Long userId, Long uploadId) {
        ApplicationDocumentUpload upload = getOwnedUpload(userId, uploadId);
        return s3FileStorageService.createAccessUrl(
                upload.getStorageKey(), upload.getUserFileName(), upload.getMimeType(), false);
    }

    @Transactional
    public void delete(Long userId, Long uploadId) {
        ApplicationDocumentUpload upload = getOwnedUpload(userId, uploadId);
        s3FileStorageService.delete(upload.getStorageKey());
        uploadRepository.delete(upload);
    }

    private ApplicationDocumentUpload getOwnedUpload(Long userId, Long uploadId) {
        return uploadRepository.findByUploadIdAndApplicationUserUserId(uploadId, userId)
                .orElseThrow(() -> new IllegalArgumentException("업로드 파일을 찾을 수 없습니다."));
    }
}
