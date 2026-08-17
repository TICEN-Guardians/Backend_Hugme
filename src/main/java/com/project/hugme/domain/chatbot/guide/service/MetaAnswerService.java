package com.project.hugme.domain.chatbot.guide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaAnswerService {

    private final ChatClient chatClient;  // conversationalChatClient

    public String generate(String sessionId, String query) {
        String prompt = """
                당신은 Hugme 서비스 내의 상담 챗봇입니다. Hugme는 HUG(주택도시보증공사) 연계
                전세보증금 리스크 진단 및 안심전세 상담 서비스입니다.
                당신은 전세보증금 관련 보증상품 안내와 전세사기 예방 상담, 허그미 내 타 서비스 문의 등을 도와줍니다.
                
                사용자가 당신에 대해 묻거나(무슨 챗봇인지, 뭘 도와줄 수 있는지),
                이전 대화 내용을 물어보면 대화 맥락을 참고해서 친절하게 답하세요.
                
                질문: %s
                """.formatted(query);

        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .user(prompt)
                .call()
                .content();
    }
}
