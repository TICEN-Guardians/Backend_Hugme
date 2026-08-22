package com.project.hugme.domain.chatbot.document.controller;

import com.project.hugme.domain.chatbot.document.dto.DocumentChatResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.service.DocumentChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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

}
