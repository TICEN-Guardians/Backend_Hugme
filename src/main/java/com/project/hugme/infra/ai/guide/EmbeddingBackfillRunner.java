package com.project.hugme.infra.ai.guide;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBackfillRunner {

    @Qualifier("chatbotJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    private final EmbeddingModel embeddingModel;

    @EventListener(ApplicationReadyEvent.class)
    public void backfillMissingEmbeddings() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, content FROM vector_store WHERE embedding IS NULL"
        );

        if (rows.isEmpty()) {
            log.info("임베딩 백필 대상 없음");
            return;
        }

        log.info("임베딩 백필 대상 {}건, 계산 시작", rows.size());

        for (Map<String, Object> row : rows) {
            String id = String.valueOf(row.get("id"));
            String content = (String) row.get("content");
            float[] embedding = embeddingModel.embed(content);

            jdbcTemplate.update(
                    "UPDATE vector_store SET embedding = ?::vector WHERE id = ?::uuid",
                    vectorToString(embedding), id
            );
        }

        log.info("임베딩 백필 완료: {}건", rows.size());
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
