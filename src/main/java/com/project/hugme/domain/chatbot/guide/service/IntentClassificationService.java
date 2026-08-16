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
            
            분류 기준:
            - category가 "product"이면 1~%d번(product 파일) 중 관련된 번호 최대 5개를 sourceIndices에
            - category가 "prevention"이면 %d~%d번(prevention 파일) 중 관련된 번호 최대 5개를 sourceIndices에
            - category가 "feature"이면 sourceIndices는 빈 배열, featureType은 아래 기능 중 하나
            
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