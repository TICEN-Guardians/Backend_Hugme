package com.project.hugme.domain.chatbot.document.controller;

import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationUpdateRequest;
import com.project.hugme.domain.chatbot.document.service.DocumentChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications/{applicationId}/documents")
@Tag(
        name = "서류 안내 챗봇",
        description = "서류 안내 챗봇 API"
)
public class DocumentPreparationController {

    private final DocumentChatService documentChatService;

    @GetMapping("/preparation")
    @Operation(
            summary = "서류 및 준비 상태 조회",
            description = "특정 신청에서 준비해야 하는 서류 목록과 준비 여부를 반환합니다."
    )
    public ResponseEntity<DocumentPreparationResponse> getPreparationStatus(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                documentChatService.getPreparationStatus(userId, applicationId)
        );
    }

    @PutMapping("/{documentId}/preparation")
    @Operation(
            summary = "서류 준비 상태 변경",
            description = "특정 신청에 포함된 서류의 준비 여부를 변경합니다."
    )
    public ResponseEntity<DocumentPreparationResponse> updatePreparationStatus(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentPreparationUpdateRequest request
    ) {
        return ResponseEntity.ok(
                documentChatService.updatePreparationStatus(
                        userId,
                        applicationId,
                        documentId,
                        request
                )
        );
    }
}
