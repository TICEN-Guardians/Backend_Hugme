package com.project.hugme.domain.diagnosis.dto.internal;

import com.project.hugme.domain.diagnosis.dto.request.DiagnosisWhatIfRequest;
import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FastApiDiagnosisWhatIfRequestTest {

    @Test
    void buildFromStoredDiagnosisValues() {
        Diagnosis diagnosis = Diagnosis.create(
                null,
                DiagnosisMode.DETAILED,
                null,
                null
        );
        diagnosis.updateDetails(
                "서울특별시 중구 장충동 1",
                null,
                null,
                200000000L,
                LocalDate.of(2026, 8, 23),
                new BigDecimal("84.00"),
                null,
                3,
                "임대인",
                null,
                false
        );
        FastApiDiagnosisResponse response = new FastApiDiagnosisResponse(
                1L,
                "DETAILED",
                "COMPLETED",
                Instant.parse("2026-08-23T00:00:00Z"),
                null,
                null,
                new FastApiDiagnosisResponse.Valuation(
                        400000000L,
                        180000000L
                ),
                new FastApiDiagnosisResponse.Indicators(
                        50.0,
                        11.11,
                        300000000L,
                        75.0,
                        300000000L,
                        0L,
                        100000000L,
                        null
                ),
                null,
                new FastApiDiagnosisResponse.Risk(
                        80,
                        49,
                        "CRITICAL",
                        new FastApiDiagnosisResponse.Breakdown(
                                18,
                                31,
                                3,
                                0,
                                31,
                                null,
                                null,
                                null,
                                null
                        ),
                        new FastApiDiagnosisResponse.Weights(
                                45,
                                45,
                                10,
                                100,
                                null,
                                null,
                                null,
                                null
                        ),
                        80,
                        List.of("OWNER_MISMATCH"),
                        true,
                        false
                ),
                List.of("OWNER_MISMATCH"),
                List.of(),
                "HIGH",
                List.of(),
                List.of(),
                "진단 결과",
                null
        );
        DiagnosisWhatIfRequest request = new DiagnosisWhatIfRequest(
                180000000L,
                10,
                5,
                70000000L,
                true
        );

        FastApiDiagnosisWhatIfRequest result =
                FastApiDiagnosisWhatIfRequest.from(
                        diagnosis,
                        response,
                        request
                );

        assertThat(result.baselineDeposit()).isEqualTo(200000000L);
        assertThat(result.scenarioDeposit()).isEqualTo(180000000L);
        assertThat(result.activeMaxClaimAmount()).isEqualTo(100000000L);
        assertThat(result.scenarioActiveMaxClaimAmount())
                .isEqualTo(70000000L);
        assertThat(result.marketTrendScore()).isEqualTo(3);
        assertThat(result.unresolvedRiskReasons())
                .containsExactly("OWNER_MISMATCH");
    }
}
