package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentClassificationService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public record RouteResult(String category, FeatureType featureType, String rewrittenQuery) {}
    private record RawRoute(String category, String featureType, String rewrittenQuery) {}

    public RouteResult route(String query, String sessionId) {

        // 최근 대화 이력 조회
        List<Message> history = chatMemory.get(sessionId);
        String historyText = history.isEmpty()
                ? "(이전 대화 없음)"
                : history.stream()
                  .map(m -> (m instanceof UserMessage ? "사용자: " : "챗봇: ") + m.getText())
                  .collect(Collectors.joining("\n"));

        String featureList = Arrays.stream(FeatureType.values())
                .map(f -> f.name() + ": " + f.description())
                .collect(Collectors.joining("\n"));

        String promptText = """
        사용자 질문을 분석해서 category, featureType, rewrittenQuery를 판단하세요.

        [카테고리 정의]
        - product: 전세보증금반환보증, 전세금안심대출보증, 특례반환보증 등 보증상품의 종류·가입조건(자격/한도)·보증료에 관한 질문
          (선순위채권, 부채비율, 전세목적물, 보증채권자 등 보증상품 약관/조건에 등장하는 용어의 뜻을 묻는 질문도 포함)
        - prevention: 전세사기 예방을 위한 일반적인 지식, 계약 시 유의사항, 등기부등본·건축물대장 등 서류의 "내용을 읽고 해석하는 방법",
          사기 유형 사례, 피해 발생 시 대처방법 등 "지식·방법"을 알고 싶어하는 질문
          (등기부등본, 확정일자, 대항력 등 용어의 의미나 "읽는 법/확인 항목"을 묻는 질문 포함.
           단, 그 서류를 "발급받는 절차"를 묻는 질문은 prevention이 아니라 feature)
        - feature: 사용자의 특정 매물·계약·상황이 안전한지 판정·진단해달라는 요청,
          또는 제출 서류 목록·준비 상태·발급 절차를 안내받고 싶어하는 요청
          (예: "이 집 괜찮나요?", "제 매물 위험도 확인해주세요", "서류는 어떻게 발급받나요?",
               "전세보증금반환보증 신청 서류가 뭐예요?", "등기부등본 어디서 떼요?")
        - meta: 챗봇 자신에 대한 질문(무슨 챗봇인지, 뭘 할 수 있는지) 또는 이전 대화 내용 등 상담 챗봇 이용에 있어 필요한 사항을 묻는 질문
        - off_topic: 전세보증금, 부동산 계약, 전세사기 예방 등 상담 챗봇의 기능과 무관한 질문(잡담, 허그미 외 서비스 문의, 부동산 관련 외적인 일반 상식 질문 등)

        - category가 "feature"이면 featureType은 아래 기능 목록 중 하나로 판단
        - category가 "product" 또는 "prevention"이면 featureType은 null
        - category가 "meta" 또는 "off_topic"이면 featureType은 null

        [product vs prevention vs feature(서류) 구분 우선순위]
        - "가입 자격·조건·한도·보증료가 얼마냐"를 묻는 질문 → product
        - "그 서류를 어떻게 읽고 뭘 확인하느냐"를 묻는 질문 → prevention
        - "그 서류가 뭐가 필요한지 / 어디서·어떻게 발급받는지 / 준비했는지 체크하고 싶은지"를 묻는 질문 → feature
        - 상품명이나 서류명이 같이 언급돼도 이 구분 기준(질문의 핵심 동사·의도)이 우선입니다.
          단어(상품명, "등기부등본" 등)만 보고 판단하지 말고, 동사를 기준으로 판단하세요:
          필요하다/준비하다/제출하다/발급받다/떼다 → feature,
          읽다/확인하다/보다 → prevention,
          조건이다/얼마다/가입하다 → product.

        [최근 대화]
        %s

        [분류 및 재작성 방법]
        - 위 [최근 대화]를 참고해서 이번 질문의 지시어·생략된 주어를 보충한 독립적인 질문(rewrittenQuery)을 만드세요.
          예: 직전에 "전세목적물"이라는 용어가 언급됐고 이번 질문이 "그게 뭐에요?"라면
              rewrittenQuery는 "전세목적물이 무엇인가요?"가 아니라
              "전세보증금반환보증에서 전세목적물이란 무엇을 의미하나요?"처럼
              원래 문서 검색이 가능한 수준으로 구체화하세요.
        - 이전 대화가 없거나 이번 질문 자체로 이미 독립적이면, rewrittenQuery는 원래 질문을 그대로 사용하세요.
        - "아뇨", "그건 됐고" 같은 표현은 직전 봇 제안에 대한 거절일 뿐, 상담 이탈 신호가 아닙니다.
          이어지는 내용으로 새 주제 질문을 하면 그 주제 기준으로 category를 판단하세요.
        - [최근 대화]에 이미 등장한 용어나 개념을 이번 질문이 다시 물어보는 것이라면(예: "그거 뭐예요?", "OO이 뭔가요?" 등),
          그 용어가 나왔던 이전 답변의 category를 이어받아 판단하세요.
          용어 자체가 일반적인 단어처럼 보여도(예: "부채비율", "확정일자") [최근 대화]에서 보증상품이나
          전세사기 예방 맥락으로 쓰였다면 off_topic이 아니라 그 맥락(product/prevention)으로 분류하세요.

        [feature 목록]
        %s

        질문: %s
        """.formatted(historyText, featureList, query);

        try {
            RawRoute raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().temperature(0.0))
                    .user(promptText)
                    .call()
                    .entity(RawRoute.class);

            FeatureType featureType = parseFeatureType(raw.featureType());

            // rewrittenQuery가 비어있거나 누락되면 원문으로 안전하게 폴백
            String rewrittenQuery = (raw.rewrittenQuery() == null || raw.rewrittenQuery().isBlank())
                    ? query
                    : raw.rewrittenQuery();

            RouteResult result = new RouteResult(raw.category(), featureType, rewrittenQuery);
            log.info("질문: '{}' -> category={}, featureType={}, rewrittenQuery='{}'",
                    query, result.category(), result.featureType(), result.rewrittenQuery());
            return result;
        } catch (Exception e) {
            log.error("라우팅 실패, 기본값으로 폴백: {}", query, e);
            // rewrittenQuery도 반드시 채워야 컴파일 및 이후 search() 호출이 안전함
            return new RouteResult("product", null, query);
        }
    }

    private FeatureType parseFeatureType(String raw) {
        if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return FeatureType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 featureType 값: {}", raw);
            return null;
        }
    }
}
