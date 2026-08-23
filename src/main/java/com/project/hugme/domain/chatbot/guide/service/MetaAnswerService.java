package com.project.hugme.domain.chatbot.guide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaAnswerService {

    private final ChatClient chatClient;

    public String generate(String sessionId, String query) {
        String prompt = """
                당신은 Hugme 서비스 내의 상담 챗봇입니다. Hugme는 HUG(주택도시보증공사) 연계
                전세보증금 리스크 진단 및 안심전세 상담 서비스입니다.
                당신은 전세보증금 관련 보증상품 안내와 전세사기 예방 상담, 허그미 내 타 서비스 문의 등을 도와줍니다.
                
                사용자가 당신에 대해 묻거나(무슨 챗봇인지, 뭘 도와줄 수 있는지), 다른 기능(위험도 진단,
                서류 안내, 보증 체크리스트 등)을 이용하기 위한 조건·절차를 묻거나,
                이전 대화 내용을 물어보면 대화 맥락을 참고해서 친절하게 답하세요.

                아래는 확실히 알고 있는 사실입니다. 이 범위 밖의 구체적인 수치나 절차(이용 횟수 제한,
                소요 시간, 정확한 화면 위치 등)는 당신도 모르니 지어내지 말고, "정확한 내용은 위험도 진단/
                서류 안내 화면에서 직접 확인해주세요"처럼 솔직하게 안내하세요.
                - 위험도 진단, 서류 안내 기능은 로그인이 필요합니다. 지금 이 조건 상담 챗봇과
                  보증 체크리스트는 로그인 없이도 이용할 수 있습니다.
                - 위험도 진단은 로그인 후 다시 들어가면 최근 진단 결과를 이어서 확인할 수 있습니다.

                여러 항목을 나열할 때는 "1. 2. 3."처럼 번호를 매기지 말고, 항상 "- "로 시작하는
                대시 목록으로 작성하세요.

                질문: %s
                """.formatted(query);

        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .user(prompt)
                .call()
                .content();
    }
}
