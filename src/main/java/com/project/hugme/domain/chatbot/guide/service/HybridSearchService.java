package com.project.hugme.domain.chatbot.guide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HybridSearchService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public List<Document> search(String query, List<String> sources) {
        if (sources.isEmpty()) {
            return List.of();
        }

        String sourceListForFilter = sources.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(","));

        List<Document> vectorResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .filterExpression("source in [" + sourceListForFilter + "]")
                        .build()
        );

        List<Map<String, Object>> bm25Results = bm25SearchBySources(query, sources);

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
                .limit(5)
                .map(e -> idToDoc.get(e.getKey()))
                .toList();
    }

    private List<Map<String, Object>> bm25SearchBySources(String query, List<String> sources) {
        return jdbcTemplate.query(
                "SELECT id, content, metadata->>'source' AS source FROM vector_store " +
                        "WHERE content::pdb.ngram(2,3) @@@ ? " +
                        "AND metadata->>'source' = ANY(?) " +
                        "ORDER BY paradedb.score(id) DESC LIMIT 5",
                (rs, rowNum) -> Map.of(
                        "id", rs.getString("id"),
                        "content", rs.getString("content"),
                        "source", rs.getString("source")
                ),
                query, sources.toArray(new String[0])
        );
    }
}