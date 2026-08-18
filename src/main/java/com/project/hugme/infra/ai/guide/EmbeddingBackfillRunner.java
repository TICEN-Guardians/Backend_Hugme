package com.project.hugme.infra.ai.guide;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBackfillRunner {

    @Qualifier("chatbotJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    private final EmbeddingModel embeddingModel;

    private static final int BATCH_SIZE = 50;

    @EventListener(ApplicationReadyEvent.class)
    public void backfillMissingEmbeddings() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, content FROM vector_store WHERE embedding IS NULL"
        );

        if (rows.isEmpty()) {
            log.info("임베딩 백필 대상 없음");
            return;
        }

        log.info("임베딩 대상 {}건, 배치 크기 {}로 계산 시작", rows.size(), BATCH_SIZE);

        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            List<Map<String, Object>> batch = rows.subList(i, Math.min(i + BATCH_SIZE, rows.size()));

            List<String> ids = new ArrayList<>();
            List<String> contents = new ArrayList<>();
            for (Map<String, Object> row : batch) {
                ids.add(String.valueOf(row.get("id")));
                contents.add((String) row.get("content"));
            }

            List<float[]> embeddings = embeddingModel.embed(contents);

            for (int j = 0; j < ids.size(); j++) {
                jdbcTemplate.update(
                        "UPDATE vector_store SET embedding = ?::vector WHERE id = ?::uuid",
                        vectorToString(embeddings.get(j)), ids.get(j)
                );
            }

            log.info("배치 처리 완료: {}/{}", Math.min(i + BATCH_SIZE, rows.size()), rows.size());
        }

        log.info("임베딩 완료: {}건", rows.size());
    }

    private String vectorToString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        return sb.append("]").toString();
    }
}