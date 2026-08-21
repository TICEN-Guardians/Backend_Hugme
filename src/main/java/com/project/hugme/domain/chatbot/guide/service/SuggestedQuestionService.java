package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuggestedQuestionService {

    private final ChatClient chatClient;

    /**
     * 사용자가 "추천 질문 줘"처럼 직접 요청했을 때, 지금까지의 대화 맥락을 바탕으로 후속 질문을 만든다.
     * 대화 이력이 없으면 빈 리스트를 반환하고, 호출부가 entry-questions로 폴백한다.
     */
    public List<String> suggestFromHistory(List<Message> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        String historyText = history.stream()
                .map(m -> (m instanceof UserMessage ? "사용자: " : "챗봇: ") + m.getText())
                .collect(Collectors.joining("\n"));

        String featureList = Arrays.stream(FeatureType.values())
                .map(f -> "- " + f.label() + ": " + f.description())
                .collect(Collectors.joining("\n"));

        String prompt = """
        아래는 사용자와 챗봇이 지금까지 나눈 대화입니다.
        사용자가 "다른 질문 추천해줘"처럼 추천 질문을 직접 요청했습니다.
        이 대화에서 이미 다룬 내용과 관련해서, 사용자가 이어서 물어볼 만한 질문을 최대 3개 제안하세요.
        [대화]에 등장하지 않은 새로운 사실이나 서류·절차를 지어내지 말고,
        이미 다룬 주제를 더 깊이 파고들거나 자연스럽게 이어지는 질문만 만드세요.
        반드시 사용자가 챗봇에게 보낼 법한 질문 형태로 작성하세요.

        답변 내용과 관련된 기능이 아래 목록에 있다면, 그 기능을 이용해보라고 권하는
        형태의 질문 하나를 포함하세요. 관련 기능이 없으면 억지로 넣지 마세요.

        [이용 가능한 기능]
        %s

        [대화]
        %s

        각 질문을 줄바꿈으로 구분해서, 질문 텍스트만 나열하세요. 제안할 게 없으면 빈 문자열로 답하세요.
        """.formatted(featureList, historyText);

        String result = chatClient.prompt().user(prompt).call().content();

        return Arrays.stream(result.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .limit(3)
                .toList();
    }

    public List<String> suggest(String query, String answer, List<Document> contextDocs) {
        String featureList = Arrays.stream(FeatureType.values())
                .map(f -> "- " + f.label() + ": " + f.description())
                .collect(Collectors.joining("\n"));

        String context = contextDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
        아래는 사용자 질문, 챗봇의 답변, 그리고 그 답변의 근거가 된 자료입니다.
        답변을 읽은 "사용자"가 챗봇에게 다시 물어볼 만한 후속 질문을 최대 2개 제안하세요.
        절대 챗봇의 말투(되묻기, 안내, 확인 요청)를 그대로 가져오지 말고,
        반드시 사용자가 챗봇에게 보낼 법한 질문 형태로만 작성하세요.

        예시:
        - 나쁜 예(챗봇 말투): "어떤 상품을 말씀하시는 건가요?"
        - 좋은 예(사용자 질문): "전세보증금반환보증 보증료가 궁금해요"

        중요: 추천 질문은 반드시 [근거자료]에 실제로 등장하는 내용에 대해서만 만드세요.
        [근거자료]에 없는 서류·절차·조건·기능을 지어내지 마세요.
        예를 들어 [근거자료]에 "등기부등본을 발급받는 절차"만 있고 "등기부등본 발급에 필요한 서류" 같은
        내용이 없다면, 그런 존재하지 않는 개념으로 후속 질문을 만들지 마세요.

        답변 내용과 관련된 기능이 아래 목록에 있고, 그 기능이 실제로 다루는 범위 안의 내용일 때만
        그 기능을 이용해보라고 권하는 형태의 질문 하나를 포함하세요(예: "내 매물 위험도 진단 받아보기").
        관련 기능이 없거나 범위 밖이면 억지로 넣지 마세요.

        [이용 가능한 기능]
        %s

        [근거자료]
        %s

        [사용자 질문]
        %s

        [챗봇 답변]
        %s

        각 질문을 줄바꿈으로 구분해서, 질문 텍스트만 나열하세요. 제안할 게 없으면 빈 문자열로 답하세요.
        """.formatted(featureList, context, query, answer);

        String result = chatClient.prompt().user(prompt).call().content();

        return Arrays.stream(result.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .limit(2)
                .toList();
    }
}
