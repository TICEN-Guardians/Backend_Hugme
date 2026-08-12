package com.project.hugme.domain.file.controller;

import com.project.hugme.domain.file.dto.FileUploadResponse;
import com.project.hugme.domain.file.service.ApplicationDocumentUploadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final ApplicationDocumentUploadService applicationDocumentUploadService;


    @Operation(
            summary = "신청 서류 파일 업로드",
            description = "특정 신청의 준비서류 파일을 업로드합니다."
    )
    @PostMapping(
            value = "/applications/{applicationId}/documents/{documentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable("applicationId")
            Long applicationId,

            @PathVariable("documentId")
            Long documentId,

            @RequestParam("file")
            MultipartFile file
    ) {
        FileUploadResponse response =
                applicationDocumentUploadService.upload(
                        userId,
                        applicationId,
                        documentId,
                        file
                );

        return ResponseEntity.ok(response);
    }
}