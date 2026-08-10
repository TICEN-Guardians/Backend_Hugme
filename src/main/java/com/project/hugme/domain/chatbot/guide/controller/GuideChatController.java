package com.project.hugme.domain.chatbot.guide.controller;

import com.project.hugme.domain.chatbot.guide.dto.ChatRequest;
import com.project.hugme.domain.chatbot.guide.dto.ChatResponse;
import com.project.hugme.domain.chatbot.guide.service.GuideChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot/guide")
@RequiredArgsConstructor
@Tag(name = "상담 챗봇", description = "전세보증금 상담 챗봇 API")
public class GuideChatController {

    private final GuideChatService guideChatService;

    @PostMapping("/messages")
    @Operation(summary = "챗봇 메시지 전송", description = "사용자 질문을 받아 카테고리 분류, 검색, 답변 생성을 수행합니다.")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        return guideChatService.handle(request);
    }
}