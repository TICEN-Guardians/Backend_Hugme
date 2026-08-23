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
        - product: 전세보증금반환보증, 전세금안심대출보증, 특례반환보증 등 보증상품의 종류·가입조건(자격/한도)·보증료에 관한 질문.
          "전세", "월세", "반전세", "보증금" 자체가 무엇인지 묻는 가장 기초적인 임대차 용어 질문도 product에 포함됩니다
          (이 서비스는 전세 관련 서비스이므로 월세를 물어도 전세와 비교하기 위한 맥락으로 취급하세요).
          (선순위채권, 부채비율, 전세목적물, 보증채권자 등 보증상품 약관/조건에 등장하는 용어의 뜻을 묻는 질문도 포함)
        - prevention: 전세사기 예방을 위한 일반적인 지식, 계약 시 유의사항, 등기부등본·건축물대장 등 서류의
          "내용을 읽고 해석하는 방법"이나 "어디서·어떻게 발급받는지" 같은 일반 상식,
          사기 유형 사례, 피해 발생 시 대처방법 등 "지식·방법"을 알고 싶어하는 질문
          (등기부등본, 확정일자, 대항력 등 용어의 의미나 "읽는 법/확인 항목", 특정 서류 하나를
           "어디서/어떻게 떼는지" 묻는 일반적인 발급 방법 질문도 여기 포함됩니다. 예: "등기부등본 뽑는법",
           "등기부등본 어디서 떼요?" — 이런 질문은 신청 체크리스트가 아니라 일반 상식 질문입니다)
        - feature: 사용자의 특정 매물·계약·상황이 안전한지 판정·진단해달라는 요청,
          또는 "지금 신청하려는/신청 중인 특정 HUG 보증상품에 어떤 서류를 제출해야 하는지" 체크리스트를
          안내받고 싶어하는 요청. 서류안내 기능은 보증상품별 자격요건을 바탕으로 그 신청에 필요한
          제출서류 목록을 안내/체크하는 기능이지, 낱개 서류 하나의 일반적인 발급 방법을 알려주는 기능이 아닙니다.
          (예: "이 집 괜찮나요?", "제 매물 위험도 확인해주세요",
               "전세보증금반환보증 신청 서류가 뭐예요?", "이 상품 신청에 필요한 서류 체크하고 싶어요")
          단, 서류명이 등장해도 그 서류가 부동산 계약·임대차·HUG 보증 신청과 실질적으로 무관한
          일반 생활 민원 서류(혼인신고서, 출생신고서, 사망신고서, 여권, 운전면허증 등)라면 feature가 아닙니다.
          아래 [off_topic 판단 시 주의] 참고.
          가장 중요: feature는 사용자가 "그 기능(위험도 진단 도구, 서류 안내 도구)을 지금 이용하고
          싶다"는 요청일 때만 해당합니다. 그 도구 자체를 "로그인해야 쓸 수 있는지, 결과를 어디서
          다시 보는지, 몇 번까지 쓸 수 있는지, (도구 이용에) 비용이 드는지, 얼마나 걸리는지"처럼
          도구의 사용 방법·정책을 묻는 질문은 feature가 아니라 meta입니다.
          주의: 이건 오직 위험도 진단·서류 안내라는 "허그미 앱 도구" 자체에 대한 질문일 때만
          적용됩니다. "HUG 보증상품"의 가입조건·제출서류·신청기한·보증료·결제방법처럼 상품 자체에
          대한 질문은 표현이 "~해야 하나요/~되나요/~가능한가요"로 비슷해 보여도 절대 meta가 아니고,
          위 [product vs prevention vs feature(서류) 구분 우선순위]를 그대로 따라 product/prevention/
          feature 중 하나로 분류하세요. 예: "확정일자 못 받으면 신청 안 되나요?"(product),
          "보증료 카드로 낼 수 있나요?"(product), "계약 끝나기 얼마 전까지 신청해야 하나요?"(product)
          — 이런 질문들은 meta가 절대 아닙니다.
        - suggestion_request: 사용자가 다음에 뭘 물어볼 수 있는지, 추천 질문을 달라고 직접 "요청"하는 경우만 해당
          (예: "다른 질문 추천해줘", "또 뭘 물어볼 수 있어?", "질문 예시 좀 줘")
          "추천"이라는 단어가 들어가도 "그만해", "하지마", "필요없어", "됐어"처럼 추천을 거부·중단시키려는
          의사표현은 suggestion_request가 아니라 meta입니다. 단어만 보지 말고 요청인지 거부인지 반드시 구분하세요.
        - meta: 챗봇 자신에 대한 질문(무슨 챗봇인지, 뭘 할 수 있는지), 챗봇의 특정 동작(추천 질문 등)을
          그만하라거나 원치 않는다는 의사표현, "위험도 진단·서류 안내라는 허그미 앱 도구 자체"를
          이용하기 위한 조건·절차·정책을 묻는 질문(로그인 필요 여부, 결과 재조회 방법, 이용 횟수·소요시간
          등 — HUG 보증상품 자체의 가입조건·서류·보증료·결제방법 등은 여기 해당 안 됨, product/prevention/
          feature로), 또는 이전 대화 내용 등 상담 챗봇 이용에 있어 필요한 사항을 묻는 질문
        - off_topic: 전세/월세/임대차/보증금/부동산 계약과 "전혀" 관련이 없는 질문만 해당합니다
          (잡담, 날씨, 연예인, 허그미 외 다른 서비스 문의, 수학·상식 퀴즈, 의미 없는 문자열 등).
          "전세가 뭐예요", "월세는 뭐죠", "보증이 뭔가요"처럼 아무리 기초적이고 사전적인 용어라도
          전세·임대차·보증 도메인에 속하면 off_topic이 절대 아닙니다.

        - category가 "feature"이면 featureType은 아래 기능 목록 중 하나로 판단
        - category가 "product", "prevention", "suggestion_request", "meta", "off_topic"이면 featureType은 null

        [product vs prevention vs feature(서류, 위험도 진단) 구분 우선순위]
        - "가입 자격·조건·한도·보증료가 얼마냐"를 묻는 질문 → product
        - "특정 서류 하나를 어떻게 읽고/뭘 확인하고/어디서·어떻게 발급받느냐"를 묻는 질문 → prevention
          (발급 방법도 "그 서류에 대한 일반 상식"이라 prevention입니다. 예: "등기부등본 어떻게 떼요?",
           "건축물대장은 어디서 발급받나요?")
        - "지금 신청하려는 이 보증상품에 어떤 서류들이 필요한지 체크리스트로 안내받고 싶다"는 질문 → feature
          (예: "전세보증금반환보증 신청 서류가 뭐예요?", "제출서류 다 준비됐는지 확인하고 싶어요")
        - 상품명이나 서류명이 같이 언급돼도 이 구분 기준(신청 체크리스트를 원하는지 vs 낱개 서류 상식을
          원하는지)이 우선입니다. 동사만 보고 기계적으로 판단하지 마세요 — "발급받다/떼다"가 쓰였어도
          특정 서류 "하나"의 일반적인 발급 방법을 묻는 것이면 prevention이고, "이 상품 신청에 필요한
          서류 전체 목록/체크"를 원하는 것이면 feature입니다.
        - "전세", "월세", "반전세", "보증금", "임대인", "임차인"처럼 도메인의 가장 기초적인 용어를
          묻는 질문도 반드시 product 또는 prevention 중 하나로 분류하세요.
          off_topic과 그 외 카테고리 사이에서 애매하면 off_topic으로 보내지 말고
          product로 분류하세요(기본값).

        [off_topic 판단 시 주의: 서류명이 등장하는 질문]
        - 서류를 "발급받다/떼다/준비하다"는 동사가 쓰였다고 무조건 off_topic이 아니라고 단정하지 마세요.
          먼저 그 서류가 부동산 계약·임대차·HUG 보증 신청과 실질적으로 연결되는지 판단하세요.
        - 등기부등본, 건축물대장, 임대차계약서처럼 태생적으로 부동산/임대차에 속하는 서류,
          또는 가족관계증명서·건강보험자격득실확인서처럼 HUG 보증상품의 특정 자격 요건(신혼부부 특례 등)
          증빙으로 실제 쓰이는 서류는 off_topic이 아니라 prevention 또는 feature입니다
          (둘 중 어느 쪽인지는 위 [product vs prevention vs feature(서류) 구분 우선순위]를 따르세요 —
           낱개 서류의 일반 발급 상식이면 prevention, 특정 보증상품 신청 체크리스트를 원하면 feature).
        - 혼인신고서, 출생신고서, 사망신고서, 여권, 운전면허증처럼 부동산·보증과 무관한
          일반 생활 민원 서류는, [최근 대화]에서 이미 특정 보증상품의 자격요건 맥락(예: 신혼부부 특례)이
          먼저 언급된 게 아니라면 off_topic으로 분류하세요.

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

            String rewrittenQuery = (raw.rewrittenQuery() == null || raw.rewrittenQuery().isBlank())
                    ? query
                    : raw.rewrittenQuery();

            RouteResult result = new RouteResult(raw.category(), featureType, rewrittenQuery);
            log.info("질문: '{}' -> category={}, featureType={}, rewrittenQuery='{}'",
                    query, result.category(), result.featureType(), result.rewrittenQuery());
            return result;
        } catch (Exception e) {
            log.error("라우팅 실패, 기본값으로 폴백: {}", query, e);
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
