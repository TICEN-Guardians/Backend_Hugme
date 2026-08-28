package com.project.hugme.domain.chatbot.guide.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

// HybridSearchService의 RRF 로직을 그대로 복제해 top-k 조합별 결과를 눈으로 비교하기 위한 수동 실행용 테스트.
// 프로덕션 코드(HybridSearchService)는 건드리지 않는다. CI에서는 돌지 않도록 기본 비활성화.
@SpringBootTest
class HybridSearchTopKSweepTest {

    private static final List<String> CATEGORIES = List.of("product", "prevention");
    private static final int QUERIES_PER_CATEGORY = 5;
    private static final int RRF_K = 60;

    private static final List<TopKCombo> COMBOS = List.of(
            new TopKCombo("8/8/5 (old)", 8, 8, 5),
            new TopKCombo("10/10/6", 10, 10, 6),
            new TopKCombo("12/12/8 (current)", 12, 12, 8),
            new TopKCombo("14/14/10", 14, 14, 10)
    );

    @Autowired
    private VectorStore vectorStore;

    @Qualifier("chatbotJdbcTemplate")
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AnswerGenerationService answerGenerationService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    //@Disabled("실제 벡터스토어/BM25를 여러 조합으로 반복 호출하는 수동 비교용 테스트. 필요할 때만 주석 해제 후 실행")
    void topKSweepRetrievalOnlyTest() {

        List<QueryCase> queryCases = collectQueryCases();

        assumeTrue(!queryCases.isEmpty(),
                "guide_chat_histories에 product/prevention 카테고리 이력이 없어 테스트를 건너뜁니다.");

        Map<String, List<Double>> overlapByComboPair = new LinkedHashMap<>();
        Map<String, List<Long>> latencyByCombo = new LinkedHashMap<>();

        for (QueryCase queryCase : queryCases) {

            System.out.println("\n===== [" + queryCase.category() + "] 질문: \"" + queryCase.question() + "\" =====");

            Map<String, HybridSearchOutcome> outcomesByCombo = new LinkedHashMap<>();

            for (TopKCombo combo : COMBOS) {

                HybridSearchOutcome outcome = runHybridSearch(queryCase.question(), queryCase.category(), combo);
                outcomesByCombo.put(combo.label(), outcome);

                latencyByCombo.computeIfAbsent(combo.label(), key -> new ArrayList<>()).add(outcome.latencyMs());

                printRankedDocs(combo.label(), outcome);
            }

            for (int i = 0; i < COMBOS.size() - 1; i++) {

                String labelA = COMBOS.get(i).label();
                String labelB = COMBOS.get(i + 1).label();

                double overlap = jaccard(outcomesByCombo.get(labelA), outcomesByCombo.get(labelB));

                String pairKey = labelA + " vs " + labelB;
                overlapByComboPair.computeIfAbsent(pairKey, key -> new ArrayList<>()).add(overlap);

                System.out.printf("[overlap] %s: %.1f%%%n", pairKey, overlap * 100);
            }
        }

        System.out.println("\n===== 전체 요약 (질문 " + queryCases.size() + "건) =====");

        for (Map.Entry<String, List<Double>> entry : overlapByComboPair.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            System.out.printf("[평균 overlap] %s = %.1f%%%n", entry.getKey(), avg * 100);
        }

        for (Map.Entry<String, List<Long>> entry : latencyByCombo.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
            System.out.printf("[평균 latency] %s = %.0fms (콤보 실행 순서가 항상 고정이라 워밍업 편향 있음, 참고만 할 것)%n",
                    entry.getKey(), avg);
        }
    }

    @Test
    //@Disabled("조합 x 질문 조합만큼 실제 OpenAI(gpt-4o-mini) 호출이 발생하는 유료 테스트. 답변 비교가 필요할 때만 주석 해제 후 실행")
    void topKAnswerCompareTest() {

        List<QueryCase> queryCases = collectQueryCases();

        assumeTrue(!queryCases.isEmpty(),
                "guide_chat_histories에 product/prevention 카테고리 이력이 없어 테스트를 건너뜁니다.");

        for (QueryCase queryCase : queryCases) {

            System.out.println("\n===== [" + queryCase.category() + "] 질문: \"" + queryCase.question() + "\" =====");

            Map<String, HybridSearchOutcome> outcomesByCombo = new LinkedHashMap<>();

            for (TopKCombo combo : COMBOS) {

                HybridSearchOutcome outcome = runHybridSearch(queryCase.question(), queryCase.category(), combo);
                outcomesByCombo.put(combo.label(), outcome);

                String sources = outcome.ranked().stream()
                        .map(ranked -> String.valueOf(ranked.document().getMetadata().getOrDefault("source", "unknown")))
                        .collect(Collectors.joining(", "));

                // 콤보마다 세션ID를 새로 발급해 ChatMemory(인메모리, 세션 스코프)가 서로 섞이지 않게 한다.
                String sessionId = "sweep-" + UUID.randomUUID();

                long answerStartNanos = System.nanoTime();
                String answer = answerGenerationService
                        .generate(sessionId, queryCase.question(), outcome.documents());
                long answerLatencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - answerStartNanos);

                System.out.println("--- " + combo.label()
                        + " (검색=" + outcome.latencyMs() + "ms, 답변생성=" + answerLatencyMs + "ms) ---");
                System.out.println("문서: " + sources);
                System.out.println("답변: " + answer);
            }

            for (int i = 0; i < COMBOS.size() - 1; i++) {

                String labelA = COMBOS.get(i).label();
                String labelB = COMBOS.get(i + 1).label();

                double overlap = jaccard(outcomesByCombo.get(labelA), outcomesByCombo.get(labelB));

                System.out.printf("[overlap] %s vs %s: %.1f%%%n", labelA, labelB, overlap * 100);
            }
        }
    }

    private List<QueryCase> collectQueryCases() {

        List<QueryCase> queryCases = new ArrayList<>();

        for (String category : CATEGORIES) {

            List<String> recentQuestions = entityManager.createQuery(
                            "SELECT h.question FROM GuideChatHistory h " +
                                    "WHERE h.category = :category ORDER BY h.createdAt DESC",
                            String.class
                    )
                    .setParameter("category", category)
                    .setMaxResults(20)
                    .getResultList();

            recentQuestions.stream()
                    .distinct()
                    .limit(QUERIES_PER_CATEGORY)
                    .forEach(question -> queryCases.add(new QueryCase(category, question)));
        }

        return queryCases;
    }

    private HybridSearchOutcome runHybridSearch(String query, String category, TopKCombo combo) {

        long startNanos = System.nanoTime();

        List<Document> vectorResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(combo.vectorTopK())
                        .filterExpression("category == '" + category + "'")
                        .build()
        );

        List<Map<String, Object>> bm25Results = bm25SearchByCategory(query, category, combo.bm25Limit());

        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Document> idToDoc = new HashMap<>();

        for (int rank = 0; rank < vectorResults.size(); rank++) {
            Document doc = vectorResults.get(rank);
            idToDoc.put(doc.getId(), doc);
            rrfScores.merge(doc.getId(), 1.0 / (RRF_K + rank + 1), Double::sum);
        }
        for (int rank = 0; rank < bm25Results.size(); rank++) {
            Map<String, Object> row = bm25Results.get(rank);
            String id = (String) row.get("id");
            String content = (String) row.get("content");
            String source = (String) row.get("source");
            idToDoc.putIfAbsent(id, new Document(content, Map.of("source", source)));
            rrfScores.merge(id, 1.0 / (RRF_K + rank + 1), Double::sum);
        }

        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        List<RankedDoc> ranked = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(combo.finalLimit())
                .map(entry -> new RankedDoc(entry.getKey(), idToDoc.get(entry.getKey()), entry.getValue()))
                .toList();

        return new HybridSearchOutcome(ranked, latencyMs);
    }

    private List<Map<String, Object>> bm25SearchByCategory(String query, String category, int limit) {
        return jdbcTemplate.query(
                "SELECT id, content, metadata->>'source' AS source FROM vector_store " +
                        "WHERE content::pdb.ngram(2,3) @@@ ? " +
                        "AND metadata->>'category' = ? " +
                        "ORDER BY paradedb.score(id) DESC LIMIT " + limit,
                (rs, rowNum) -> Map.of(
                        "id", rs.getString("id"),
                        "content", rs.getString("content"),
                        "source", rs.getString("source")
                ),
                query, category
        );
    }

    private void printRankedDocs(String comboLabel, HybridSearchOutcome outcome) {

        System.out.println("--- " + comboLabel + " (latency=" + outcome.latencyMs() + "ms) ---");

        int rank = 1;
        for (RankedDoc rankedDoc : outcome.ranked()) {

            String source = String.valueOf(rankedDoc.document().getMetadata().getOrDefault("source", "unknown"));

            System.out.printf(
                    " %2d. id=%s | source=%s | score=%.4f%n",
                    rank++, rankedDoc.key(), source, rankedDoc.score()
            );
        }
    }

    private double jaccard(HybridSearchOutcome a, HybridSearchOutcome b) {

        Set<String> keysA = a.ranked().stream().map(RankedDoc::key).collect(Collectors.toSet());
        Set<String> keysB = b.ranked().stream().map(RankedDoc::key).collect(Collectors.toSet());

        if (keysA.isEmpty() && keysB.isEmpty()) {
            return 1.0;
        }

        Set<String> union = new HashSet<>(keysA);
        union.addAll(keysB);

        Set<String> intersection = new HashSet<>(keysA);
        intersection.retainAll(keysB);

        return (double) intersection.size() / union.size();
    }

    private record TopKCombo(String label, int vectorTopK, int bm25Limit, int finalLimit) {
    }

    private record QueryCase(String category, String question) {
    }

    private record RankedDoc(String key, Document document, double score) {
    }

    private record HybridSearchOutcome(List<RankedDoc> ranked, long latencyMs) {
        List<Document> documents() {
            return ranked.stream().map(RankedDoc::document).toList();
        }
    }
}
