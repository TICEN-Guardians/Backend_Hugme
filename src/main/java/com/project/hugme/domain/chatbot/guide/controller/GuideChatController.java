package com.project.hugme.domain.chatbot.guide.controller;

import com.project.hugme.domain.auth.security.CustomUserDetails;
import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
import com.project.hugme.domain.chatbot.guide.dto.EntryQuestion;
import com.project.hugme.domain.chatbot.guide.dto.GuideChatHistoryDto;
import com.project.hugme.domain.chatbot.guide.repository.GuideChatHistoryRepository;
import com.project.hugme.domain.chatbot.guide.service.GuideChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    private final GuideChatHistoryRepository guideChatHistoryRepository;

    @PostMapping("/messages")
    @Operation(summary = "챗봇 메시지 전송", description = "사용자 질문을 받아 카테고리 분류, 검색, 답변 생성을 수행합니다.")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        Long userId = extractUserId();
        return guideChatService.handle(userId, request);
    }

    @Operation(summary = "채팅 이력 조회", description = "로그인한 사용자의 상담 챗봇 대화 이력을 시간순으로 조회합니다.")
    @GetMapping("/history")
    public List<GuideChatHistoryDto> getHistory() {
        Long userId = extractUserId();

        if (userId == null) {
            return List.of();  // 비로그인은 이력 없음
        }

        return guideChatHistoryRepository.findByUser_UserIdOrderByCreatedAtAsc(userId).stream()
                .map(h -> new GuideChatHistoryDto(
                        h.getHistoryId(), h.getCategory(), h.getQuestion(),
                        h.getAnswer(), h.getSources(), h.getCreatedAt()
                ))
                .toList();
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
        return List.of(
                new EntryQuestion(
                        "product",
                        "보증상품 문의",
                        List.of(
                                "전세보증금반환보증이 뭔가요?",
                                "전세금안심대출보증과 전세보증금반환보증은 뭐가 달라요?",
                                "보증 가입 조건이 궁금해요"
                        )
                ),
                new EntryQuestion(
                        "prevention",
                        "전세사기 예방",
                        List.of(
                                "전세사기 예방을 위해 뭘 확인해야 하나요?",
                                "등기부등본은 어떻게 확인하나요?",
                                "계약할 때 조심해야 할 점이 뭔가요?"
                        )
                ),
                new EntryQuestion(
                        "feature",
                        "다른 기능 이용",
                        List.of(
                                "제 매물이 안전한지 확인하고 싶어요",
                                "전세 계약에 필요한 서류가 궁금해요"
                        )
                )
        );
    }
}