package com.project.hugme.infra.ai.guide;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionRunner implements CommandLineRunner {

    private final VectorStore vectorStore;

    @Qualifier("chatbotJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;


    @Value("${app.documents.root}")
    private String documentsRoot;

    @Value("${app.documents.ingest-on-startup:true}")
    private boolean ingestOnStartup;

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "product", "product",
            "prevention", "prevention"
    );

    @Override
    public void run(String... args) throws Exception {
        if (!ingestOnStartup) {
            log.info("재적재 스킵");
            return;
        }

        Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store", Integer.class);

        if (existingCount != null && existingCount > 0) {
            log.info("기존 데이터 {}건 삭제 후 재적재", existingCount);
            jdbcTemplate.update("DELETE FROM vector_store");
        }

        for (var entry : CATEGORY_MAP.entrySet()) {
            Path processDir = Paths.get(documentsRoot, entry.getKey(), "process");
            String category = entry.getValue();
            ingestFolder(processDir, category);
        }

        ensureBm25Index();
    }

    private void ingestFolder(Path dir, String category) {
        if (!Files.isDirectory(dir)) {
            log.warn("폴더 없음, 건너뜀: {}", dir);
            return;
        }

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".md"))
                    .forEach(p -> ingestFile(p, category));
        } catch (IOException e) {
            throw new RuntimeException("문서 폴더 읽기 실패: " + dir, e);
        }
    }

    private void ingestFile(Path filePath, String category) {
        Resource resource = new FileSystemResource(filePath);

        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .build();

        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        List<Document> documents = reader.get();

        documents.forEach(doc -> {
            doc.getMetadata().put("category", category);
            doc.getMetadata().put("source", filePath.getFileName().toString());
        });

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
        List<Document> chunks = splitter.apply(documents);

        vectorStore.add(chunks);
        log.info("적재 완료: {} ({}개 청크, category={})", filePath.getFileName(), chunks.size(), category);
    }

    private void ensureBm25Index() {
        Integer indexExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'search_idx'",
                Integer.class
        );

        if (indexExists != null && indexExists > 0) {
            log.info("BM25 인덱스(search_idx) 이미 존재, 건너뜀");
            return;
        }

        log.info("BM25 인덱스(search_idx) 생성 중...");
        jdbcTemplate.execute(
                "CREATE INDEX search_idx ON vector_store USING bm25 (id, (content::pdb.ngram(2,3))) WITH (key_field='id')"
        );
        log.info("BM25 인덱스(search_idx) 생성 완료");
    }
}