package com.project.hugme.domain.file.controller;

import com.project.hugme.domain.file.dto.FileAccessUrlResponse;
import com.project.hugme.domain.file.dto.FileUploadResponse;
import com.project.hugme.domain.file.service.ApplicationDocumentUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(
        name = "신청 서류 파일 관리",
        description = "신청별 준비서류 업로드, 검증 상태 조회, 목록 조회, 미리보기, 다운로드 및 삭제 API"
)
public class FileController {

    private final ApplicationDocumentUploadService applicationDocumentUploadService;

    @Operation(
            summary = "신청 서류 업로드",
            description = "PDF, JPEG 또는 PNG 파일을 비공개 S3에 저장하고 지원 대상 서류의 종류를 자동 검증합니다."
    )
    @PostMapping(
            value = "/applications/{applicationId}/documents/{documentId}/uploads",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                applicationDocumentUploadService.upload(userId, applicationId, documentId, file));
    }

    @Operation(
            summary = "업로드 상세 및 검증 상태 조회",
            description = "업로드 파일의 메타데이터와 서류 종류 자동 검증 결과를 조회합니다."
    )
    @GetMapping("/document-uploads/{uploadId}")
    public ResponseEntity<FileUploadResponse> getUpload(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long uploadId
    ) {
        return ResponseEntity.ok(applicationDocumentUploadService.get(userId, uploadId));
    }

    @Operation(
            summary = "신청별 업로드 서류 목록 조회",
            description = "로그인 사용자의 특정 신청에 업로드된 모든 서류를 최신순으로 조회합니다."
    )
    @GetMapping("/applications/{applicationId}/document-uploads")
    public ResponseEntity<List<FileUploadResponse>> listUploads(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(applicationDocumentUploadService.list(userId, applicationId));
    }

    @Operation(
            summary = "업로드 서류 미리보기 URL 발급",
            description = "브라우저에서 PDF 또는 이미지를 표시할 수 있는 단기 유효 S3 URL을 발급합니다."
    )
    @GetMapping("/document-uploads/{uploadId}/preview")
    public ResponseEntity<FileAccessUrlResponse> preview(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long uploadId
    ) {
        return ResponseEntity.ok(applicationDocumentUploadService.preview(userId, uploadId));
    }

    @Operation(
            summary = "업로드 서류 다운로드 URL 발급",
            description = "원본 파일명으로 내려받을 수 있는 단기 유효 S3 URL을 발급합니다."
    )
    @GetMapping("/document-uploads/{uploadId}/download")
    public ResponseEntity<FileAccessUrlResponse> download(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long uploadId
    ) {
        return ResponseEntity.ok(applicationDocumentUploadService.download(userId, uploadId));
    }

    @Operation(
            summary = "업로드 서류 삭제",
            description = "로그인 사용자가 소유한 업로드 서류를 S3와 데이터베이스에서 삭제합니다."
    )
    @DeleteMapping("/document-uploads/{uploadId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long uploadId
    ) {
        applicationDocumentUploadService.delete(userId, uploadId);
        return ResponseEntity.noContent().build();
    }
}
