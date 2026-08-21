package com.project.hugme.domain.chatbot.guide.controller;

import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
import com.project.hugme.domain.chatbot.guide.dto.EntryQuestion;
import com.project.hugme.domain.chatbot.guide.dto.GuideChatHistoryDto;
import com.project.hugme.domain.chatbot.guide.dto.SessionSummaryDto;
import com.project.hugme.domain.chatbot.guide.service.GuideChatService;
import com.project.hugme.domain.chatbot.guide.service.GuideSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot/guide")
@RequiredArgsConstructor
@Tag(name = "상담 챗봇", description = "전세보증금 상담 챗봇 API")
public class GuideChatController {

    private final GuideChatService guideChatService;
    private final GuideSessionService guideSessionService;

    @PostMapping("/messages")
    @Operation(
            summary = "챗봇 메시지 전송",
            description = "사용자 질문을 받아 카테고리 분류, 검색, 답변 생성을 수행하고, 완성된 답변을 반환합니다."
    )
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        Long userId = extractUserId();
        return guideChatService.handle(userId, request);
    }

    @Operation(summary = "세션 채팅 이력 조회", description = "로그인한 사용자의 특정 상담 세션 대화 이력을 시간순으로 조회합니다.")
    @GetMapping("/history")
    public List<GuideChatHistoryDto> getHistory(@RequestParam String sessionId) {
        Long userId = extractUserId();

        if (userId == null) {
            return List.of();  // 비로그인은 이력 없음
        }

        return guideSessionService.getSessionHistory(userId, sessionId);
    }

    @Operation(summary = "상담 세션 목록 조회", description = "로그인한 사용자의 상담 세션 목록을 최근 활동순으로 조회합니다.")
    @GetMapping("/sessions")
    public List<SessionSummaryDto> getSessions() {
        Long userId = extractUserId();

        if (userId == null) {
            return List.of();
        }

        return guideSessionService.listSessions(userId);
    }

    @Operation(summary = "상담 세션 삭제", description = "로그인한 사용자의 특정 상담 세션과 그 대화 이력을 삭제합니다.")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        Long userId = extractUserId();

        if (userId != null) {
            guideSessionService.deleteSession(userId, sessionId);
        }

        return ResponseEntity.noContent().build();
    }

    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        return null;
    }

    @Operation(summary = "초기 가이드 질문 조회", description = "챗봇 진입 시 카테고리별 대표 질문 목록을 제공합니다.")
    @GetMapping("/entry-questions")
    public List<EntryQuestion> entryQuestions() {
        return EntryQuestion.defaults();
    }
}