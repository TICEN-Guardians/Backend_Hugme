package com.project.hugme.domain.file.service;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.product.Document;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.checklist.repository.DocumentRepository;
import com.project.hugme.domain.file.dto.FileUploadResponse;
import com.project.hugme.domain.file.dto.StoredFile;
import com.project.hugme.domain.file.entity.ApplicationDocumentUpload;
import com.project.hugme.domain.file.repository.ApplicationDocumentUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationDocumentUploadService {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationDocumentUploadRepository uploadRepository;
    private final LocalStorageService localStorageService;

    @Transactional
    public FileUploadResponse upload(
            Long userId,
            Long applicationId,
            Long documentId,
            MultipartFile file
    ) {
        // 1. 로그인 사용자의 신청인지 확인
        Application application =
                applicationRepository
                        .findByApplicationIdAndUser_UserId(
                                applicationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "신청정보를 찾을 수 없습니다."
                                )
                        );

        // 2. 업로드할 서류 조회
        Document document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "서류정보를 찾을 수 없습니다."
                                )
                        );

        // 3. 실제 파일을 로컬에 저장
        StoredFile storedFile =
                localStorageService.storeFile(file);

        // 4. DB에 저장할 업로드 엔티티 생성
        ApplicationDocumentUpload upload =
                ApplicationDocumentUpload.create(
                        application,
                        document,
                        storedFile.originalFileName(),
                        storedFile.storageKey(),
                        storedFile.mimeType(),
                        storedFile.fileSize()
                );

        // 5. 업로드 정보 DB 저장
        ApplicationDocumentUpload savedUpload =
                uploadRepository.save(upload);

        // 6. 응답 DTO 반환
        return FileUploadResponse.from(savedUpload);
    }
}