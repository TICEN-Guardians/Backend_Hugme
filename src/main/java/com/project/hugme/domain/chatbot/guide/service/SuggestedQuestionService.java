package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestedQuestionService {

    private final ChatClient chatClient;

    public List<String> suggest(String query, String answer) {
        String featureList = Arrays.stream(FeatureType.values())
                .map(f -> "- " + f.label() + ": " + f.description())
                .collect(Collectors.joining("\n"));

        String prompt = """
        아래는 사용자 질문과 챗봇의 답변입니다.
        답변을 읽은 "사용자"가 챗봇에게 다시 물어볼 만한 후속 질문을 최대 2개 제안하세요.
        절대 챗봇의 말투(되묻기, 안내, 확인 요청)를 그대로 가져오지 말고,
        반드시 사용자가 챗봇에게 보낼 법한 질문 형태로만 작성하세요.
        
        예시:
        - 나쁜 예(챗봇 말투): "어떤 상품을 말씀하시는 건가요?"
        - 좋은 예(사용자 질문): "전세보증금반환보증 보증료가 궁금해요"
        
        답변 내용과 관련된 기능이 아래 목록에 있다면, 그 기능을 이용해보라고 권하는
        형태의 질문 하나를 포함하세요(예: "내 매물 위험도 진단 받아보기").
        관련 기능이 없으면 억지로 넣지 마세요.
        
        [이용 가능한 기능]
        %s
        
        [사용자 질문]
        %s
        
        [챗봇 답변]
        %s
        
        각 질문을 줄바꿈으로 구분해서, 질문 텍스트만 나열하세요. 제안할 게 없으면 빈 문자열로 답하세요.
        """.formatted(featureList, query, answer);

        String result = chatClient.prompt().user(prompt).call().content();

        return Arrays.stream(result.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .limit(2)
                .toList();
    }
}
