package com.project.hugme.domain.diagnosis.enums;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosisModeMigrationContractTest {

    private static final Path MIGRATION = Path.of(
            "src/main/resources/db/migration/rds/V8__add_diagnosis_mode_and_anonymous_access.sql"
    );

    @Test
    void migrationContainsEveryDiagnosisMode() throws IOException {
        String sql = Files.readString(MIGRATION);
        for (DiagnosisMode mode : DiagnosisMode.values()) {
            assertTrue(sql.contains("'" + mode.name() + "'"));
        }
    }
}
