package com.project.hugme.infra.ocr.enums;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SourceTypeMigrationContractTest {

    private static final Path MIGRATION = Path.of(
            "src/main/resources/db/migration/"
                    + "V6__add_pdf_llm_registry_source_type.sql"
    );

    @Test
    void migrationCheckConstraintContainsEverySourceType() throws IOException {
        String sql = Files.readString(MIGRATION);

        assertTrue(sql.contains("ALTER TABLE IF EXISTS registry_results"));
        for (SourceType type : SourceType.values()) {
            assertTrue(
                    sql.contains("'" + type.name() + "'"),
                    () -> "DB CHECK에 SourceType이 누락되었습니다: " + type.name()
            );
        }
    }
}
