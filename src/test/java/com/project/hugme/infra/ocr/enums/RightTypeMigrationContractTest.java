package com.project.hugme.infra.ocr.enums;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RightTypeMigrationContractTest {

    private static final Path MIGRATION = Path.of(
            "src/main/resources/db/migration/"
                    + "V5__add_mortgage_transfer_registry_right_type.sql"
    );

    @Test
    void migrationCheckConstraintContainsEveryRightType() throws IOException {
        String sql = Files.readString(MIGRATION);

        for (RightType type : RightType.values()) {
            assertTrue(
                    sql.contains("'" + type.name() + "'"),
                    () -> "DB CHECK에 RightType이 누락되었습니다: " + type.name()
            );
        }
    }
}
