package com.project.hugme.domain.chatbot.document.controller;

import com.project.hugme.domain.chatbot.document.dto.DocumentChatResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentChatHistoryResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentChatSessionResponse;
import com.project.hugme.domain.chatbot.document.dto.DocumentSearchRequest;
import com.project.hugme.domain.chatbot.document.service.DocumentChatService;
import com.project.hugme.domain.chatbot.document.service.DocumentChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot/documents")
@Tag(
        name = "서류 안내 챗봇",
        description = "서류 안내 챗봇 API"
)
public class DocumentChatController {

    private final DocumentChatService documentChatService;
    private final DocumentChatHistoryService documentChatHistoryService;

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

    @Operation(
            summary = "서류 안내 챗봇 세션 목록 조회",
            description = "로그인 사용자의 서류 안내 챗봇 대화 목록을 최근 활동순으로 조회합니다."
    )
    @GetMapping("/sessions")
    public ResponseEntity<List<DocumentChatSessionResponse>> getSessions(
            @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        return ResponseEntity.ok(documentChatHistoryService.listSessions(userId));
    }

    @Operation(
            summary = "서류 안내 챗봇 대화 이력 조회",
            description = "로그인 사용자의 특정 채팅 세션에 포함된 질문과 답변을 시간순으로 조회합니다."
    )
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<DocumentChatHistoryResponse>> getSessionHistory(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String sessionId
    ) {
        return ResponseEntity.ok(documentChatHistoryService.getSessionHistory(userId, sessionId));
    }

    @Operation(
            summary = "서류 안내 챗봇 세션 삭제",
            description = "로그인 사용자의 특정 채팅 세션과 모든 대화 이력을 삭제합니다."
    )
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable String sessionId
    ) {
        documentChatHistoryService.deleteSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

}
