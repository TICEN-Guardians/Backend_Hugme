package com.project.hugme.domain.diagnosis.enums;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosisStatusMigrationContractTest {

    private static final Path MIGRATION = Path.of(
            "src/main/resources/db/migration/rds/V11__align_diagnosis_status_check.sql"
    );

    @Test
    void migrationContainsEveryDiagnosisStatus() throws IOException {
        String sql = Files.readString(MIGRATION);
        for (DiagnosisStatus status : DiagnosisStatus.values()) {
            assertTrue(sql.contains("'" + status.name() + "'"));
        }
    }
}
