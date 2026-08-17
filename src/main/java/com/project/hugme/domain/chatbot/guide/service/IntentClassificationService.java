package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import com.project.hugme.domain.chatbot.guide.repository.DocumentCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentClassificationService {

    private final ChatClient chatClient;
    private final DocumentCatalogRepository documentCatalogRepository;

    public record RouteResult(String category, List<String> sources, FeatureType featureType) {}
    private record RawRoute(String category, List<Integer> sourceIndices, String featureType) {}

    public RouteResult route(String query) {
        List<String> productSources = documentCatalogRepository.getByCategory("product");
        List<String> preventionSources = documentCatalogRepository.getByCategory("prevention");
        List<String> allSources = new ArrayList<>();
        allSources.addAll(productSources);
        allSources.addAll(preventionSources);

        StringBuilder numbered = new StringBuilder();
        for (int i = 0; i < allSources.size(); i++) {
            numbered.append(i + 1).append(": ").append(allSources.get(i)).append("\n");
        }

        String featureList = Arrays.stream(FeatureType.values())
                .map(f -> f.name() + ": " + f.description())
                .collect(Collectors.joining("\n"));

        String promptText = """
        사용자 질문을 분석해서 category, sourceIndices, featureType을 판단하세요.
        
        [카테고리 정의]
        - product: 전세보증금반환보증, 전세금안심대출보증, 특례반환보증 등 보증상품의 종류·조건·가입방법·보증금액·보증료에 관한 질문
        - prevention: 전세사기 예방을 위한 일반적인 지식, 계약 시 유의사항, 등기부등본·건축물대장 등 서류를 어떻게 읽고 무엇을 확인해야 하는지, 사기 유형 사례, 피해 발생 시 대처방법 등 "지식·방법"을 알고 싶어하는 질문
        - feature: 사용자의 특정 매물·계약·상황이 안전한지 판정·진단해달라는 요청, 또는 서류 발급·제출 절차 자체를 안내받고 싶어하는 요청 (예: "이 집 괜찮나요?", "제 매물 위험도 확인해주세요", "서류는 어떻게 발급받나요?")
        - meta: 챗봇 자신에 대한 질문(무슨 챗봇인지, 뭘 할 수 있는지) 또는 이전 대화 내용 등 상담 챗봇 이용에 있어 필요한 사항을 묻는 질문
        - off_topic: 전세보증금, 부동산 계약, 전세사기 예방 등 상담 챗봇의 기능과 무관한 질문(잡담, 허그미 외 서비스 문의, 부동산 관련 외적인 일반 상식 질문 등)
        
        [분류 후 처리 방법]
        - category가 "product"이면 아래 번호 목록 중 1~%d번(product 파일)에서 관련된 번호 최대 5개를 sourceIndices에
        - category가 "prevention"이면 아래 번호 목록 중 %d~%d번(prevention 파일)에서 관련된 번호 최대 5개를 sourceIndices에
        - category가 "feature"이면 sourceIndices는 빈 배열, featureType은 아래 기능 목록 중 하나로 판단
        - category가 "feature"이면 sourceIndices는 빈 배열, featureType은 null
        - category가 "off_topic"이면 sourceIndices는 빈 배열, featureType은 null
        
        반드시 아래 번호 목록에 있는 숫자만 사용하세요. 목록에 없는 번호나 파일명을 만들어내지 마세요.
        
        [문서 번호 목록]
        %s
        
        [feature 목록]
        %s
        
        질문: %s
        """.formatted(
                productSources.size(),
                productSources.size() + 1, allSources.size(),
                numbered, featureList, query
        );

        try {
            RawRoute raw = chatClient.prompt()
                    .options(OpenAiChatOptions.builder().temperature(0.0))
                    .user(promptText)
                    .call()
                    .entity(RawRoute.class);

            List<String> validSources = raw.sourceIndices() == null ? List.of() :
                    raw.sourceIndices().stream()
                    .filter(i -> i != null && i >= 1 && i <= allSources.size())
                    .map(i -> allSources.get(i - 1))
                    .distinct()
                    .limit(5)
                    .toList();

            FeatureType featureType = parseFeatureType(raw.featureType());

            RouteResult result = new RouteResult(raw.category(), validSources, featureType);
            log.info("질문: '{}' -> category={}, sources={}, featureType={}",
                    query, result.category(), result.sources(), result.featureType());
            return result;
        } catch (Exception e) {
            log.error("라우팅 실패, 기본값으로 폴백: {}", query, e);
            return new RouteResult("product", List.of(), null);
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