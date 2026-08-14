package com.project.hugme.domain.chatbot.guide.service;

import com.project.hugme.domain.chatbot.FeatureType;
import com.project.hugme.domain.chatbot.guide.repository.DocumentCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

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

    private FeatureType classifyFeature(String query) {
        String optionList = Arrays.stream(FeatureType.values())
                .map(f -> f.name() + ": " + f.description())
                .collect(Collectors.joining("\n"));

        String prompt = """
            사용자 질문이 아래 기능 중 어떤 것을 원하는지 판단하세요.
            
            %s
            
            반드시 위 목록의 이름(예: RISK_DIAGNOSIS)만 답하세요. 다른 설명은 붙이지 마세요.
            
            질문: %s
            """.formatted(optionList, query);

        String result = chatClient.prompt().user(prompt).call().content();
        try {
            return FeatureType.valueOf(result.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("feature 세부 판별 실패: {}", result);
            return null;
        }
    }

    public RouteResult route(String query) {

        String category = classifyCategory(query);

        if ("feature".equals(category)) {
            FeatureType featureType = classifyFeature(query);
            log.info("질문: '{}' -> category=feature, featureType={}", query, featureType);
            return new RouteResult(category, List.of(), featureType);
        }
        List<String> candidates = documentCatalogRepository.getByCategory(category);
        List<String> sources = selectRelevantSources(query, candidates);
        log.info("질문: '{}' -> category={}, sources={}", query, category, sources);
        return new RouteResult(category, sources, null);
    }

    private String classifyCategory(String query) {
        String prompt = """
                사용자 질문을 다음 세 카테고리 중 하나로 분류하세요.
                
                - product: 전세보증금반환보증, 전세금안심대출보증, 특례반환보증 등 보증상품의 종류·조건·가입방법·보증금액·보증료·기타 약관에 관한 질문
                - prevention: 전세사기 예방을 위한 일반적인 지식, 계약 시 유의사항, 등기부등본 확인 방법, 사기 유형 사례, 피해 발생 시 대처방법 등 "어떻게 확인하는지/무엇을 조심해야 하는지"를 알고 싶어하는 질문
                - feature: 특정 매물·주소·본인 계약이 안전한지, 위험한지, 괜찮은지를 판정·진단해달라는 요청(예: "이 집 괜찮나요?", "여기 위험한가요?", "제 계약 확인해주세요" 등 구체적 판정을 원하는 질문), 또는 보증 보험 가입을 위한 서류 검토·안내를 요청하는 질문. 사용자가 "위험도 진단" 혹은 "서류 안내 챗봇" 등의 단어를 몰라도, 판정을 원하거나 product와 prevent에 해당하지 않는, 상담 챗봇의 메인 기능을 벗어나는 의도라면 feature로 분류하세요.

                반드시 product, prevention, feature 중 하나의 단어로만 답하세요.
                
                질문: %s
                """.formatted(query);

        String result = chatClient.prompt().user(prompt).call().content();
        String category = result.trim().toLowerCase();

        return List.of("product", "prevention", "feature").contains(category) ? category : "product";
    }

    private List<String> selectRelevantSources(String query, List<String> candidateSources) {
        if (candidateSources.isEmpty()) {
            return List.of();
        }

        String sourceList = String.join("\n", candidateSources);

        String prompt = """
                아래는 관련 카테고리에 속한 문서 파일명 목록입니다.
                파일명 자체가 문서 내용을 요약하고 있습니다.
                사용자 질문에 답하는 데 가장 관련 있는 파일을 최대 5개까지 골라주세요.
                반드시 파일명만, 쉼표로 구분해서 답하세요. 다른 설명은 붙이지 마세요.
                
                파일 목록:
                %s
                
                질문: %s
                """.formatted(sourceList, query);

        String result = chatClient.prompt().user(prompt).call().content();

        return Arrays.stream(result.trim().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}