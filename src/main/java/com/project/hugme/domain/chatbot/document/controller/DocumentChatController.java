package com.project.hugme.domain.chatbot.document.controller;

import com.project.hugme.domain.chatbot.document.dto.DocumentChatResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentPreparationUpdateRequest;
import com.project.hugme.domain.chatbot.document.service.DocumentChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot/documents")
@Tag(
        name = "서류 안내 챗봇",
        description = "서류 안내 챗봇 API"
)
public class DocumentChatController {

    private final DocumentChatService documentChatService;

    @Operation(
            summary = "챗봇 메시지 전송",
            description = "서류 번호와 사용자 질문을 받아 관련 정보를 탐색하여 답변 생성을 수행합니다."
    )
    @PostMapping("/messages")
    public ResponseEntity<DocumentChatResponse> chat(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody DocumentSearchRequest request
    ) throws Exception {

        DocumentChatResponse response =
                documentChatService.chat(userId, request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{applicationId}/preparation")
    @Operation(
            summary = "서류 및 준비 상태 조회",
            description = "준비해야 하는 서류 목록을 반환합니다."
    )
    public ResponseEntity<DocumentPreparationResponse> getPreparationStatus(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(documentChatService.getPreparationStatus(userId, applicationId));
    }

    @PutMapping("/{applicationId}/preparation/{documentId}")
    @Operation(
            summary = "서류 준비 상태 변경",
            description = "각 서류의 준비 상태를 변경합니다."
    )
    public ResponseEntity<DocumentPreparationResponse> updatePreparationStatus(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentPreparationUpdateRequest request
    ) {
        return ResponseEntity.ok(documentChatService.updatePreparationStatus(
                userId, applicationId, documentId, request));
    }
}
