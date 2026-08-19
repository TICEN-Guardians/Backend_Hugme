package com.project.hugme.domain.chatbot.guide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HybridSearchService {

    private final VectorStore vectorStore;

    @Qualifier("chatbotJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    public List<Document> search(String query, String category) {
        if (category == null || category.isBlank()) {
            return List.of();
        }

        List<Document> vectorResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(12)
                        .filterExpression("category == '" + category + "'")
                        .build()
        );

        List<Map<String, Object>> bm25Results = bm25SearchByCategory(query, category);

        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Document> idToDoc = new HashMap<>();
        int k = 60;

        for (int rank = 0; rank < vectorResults.size(); rank++) {
            Document doc = vectorResults.get(rank);
            idToDoc.put(doc.getId(), doc);
            rrfScores.merge(doc.getId(), 1.0 / (k + rank + 1), Double::sum);
        }
        for (int rank = 0; rank < bm25Results.size(); rank++) {
            Map<String, Object> row = bm25Results.get(rank);
            String id = (String) row.get("id");
            String content = (String) row.get("content");
            String source = (String) row.get("source");
            idToDoc.putIfAbsent(id, new Document(content, Map.of("source", source)));
            rrfScores.merge(id, 1.0 / (k + rank + 1), Double::sum);
        }

        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(8)
                .map(e -> idToDoc.get(e.getKey()))
                .toList();
    }

    private List<Map<String, Object>> bm25SearchByCategory(String query, String category) {
        return jdbcTemplate.query(
                "SELECT id, content, metadata->>'source' AS source FROM vector_store " +
                        "WHERE content::pdb.ngram(2,3) @@@ ? " +
                        "AND metadata->>'category' = ? " +
                        "ORDER BY paradedb.score(id) DESC LIMIT 12",
                (rs, rowNum) -> Map.of(
                        "id", rs.getString("id"),
                        "content", rs.getString("content"),
                        "source", rs.getString("source")
                ),
                query, category
        );
    }
}