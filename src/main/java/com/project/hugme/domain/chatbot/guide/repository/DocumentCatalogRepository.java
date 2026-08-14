package com.project.hugme.domain.chatbot.guide.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DocumentCatalogRepository {

    @Qualifier("chatbotJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    private Map<String, List<String>> sourcesByCategory;

    @EventListener(ApplicationReadyEvent.class)
    public void loadCatalog() {
        List<Map<String, String>> all = jdbcTemplate.query(
                "SELECT DISTINCT metadata->>'source' AS source, " +
                        "metadata->>'category' AS category " +
                        "FROM vector_store",
                (rs, rowNum) -> Map.of(
                        "source", rs.getString("source"),
                        "category", rs.getString("category")
                )
        );

        sourcesByCategory = all.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.get("category"),
                        Collectors.mapping(doc -> doc.get("source"), Collectors.toList())
                ));
    }

    public List<String> getByCategory(String category) {
        return sourcesByCategory.getOrDefault(category, List.of());
    }
}