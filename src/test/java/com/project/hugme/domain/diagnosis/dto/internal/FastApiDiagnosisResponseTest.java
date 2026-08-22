package com.project.hugme.domain.diagnosis.dto.internal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class FastApiDiagnosisResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesRiskScoreFloorContract() throws Exception {
        String json = """
                {
                  "marketComparables": {
                    "status": "AVAILABLE",
                    "source": "MOLIT_RTMS",
                    "scope": "SAME_LEGAL_DONG",
                    "sampleCount": 12,
                    "periodStart": "2026-01-03",
                    "periodEnd": "2026-06-28",
                    "areaMin": 57.2,
                    "areaMax": 62.1,
                    "minimum": 180000000,
                    "percentile25": 195000000,
                    "median": 210000000,
                    "percentile75": 225000000,
                    "maximum": 240000000,
                    "userDepositPercentile": 66.7,
                    "bins": [
                      {"lowerBound": 180000000, "upperBound": 200000000, "count": 4}
                    ],
                    "warnings": []
                  },
                  "risk": {
                    "score": 80,
                    "baseScore": 23,
                    "grade": "CRITICAL",
                    "breakdown": {
                      "priceBurden": 18,
                      "leaseMarketDeviation": 5,
                      "marketTrend": 0,
                      "policyAdjustment": 0,
                      "rightsAdjustment": 57
                    },
                    "weights": {
                      "priceBurden": 45,
                      "leaseMarketDeviation": 45,
                      "marketTrend": 10,
                      "total": 100
                    },
                    "scoreFloor": 80,
                    "floorReasons": ["OWNER_MISMATCH"],
                    "scoreFloorApplied": true,
                    "provisionalCollateralBasis": false
                  }
                }
                """;

        FastApiDiagnosisResponse response = objectMapper.readValue(
                json,
                FastApiDiagnosisResponse.class
        );

        assertThat(response.risk().score()).isEqualTo(80);
        assertThat(response.risk().baseScore()).isEqualTo(23);
        assertThat(response.risk().breakdown().rightsAdjustment()).isEqualTo(57);
        assertThat(response.risk().floorReasons()).containsExactly("OWNER_MISMATCH");
        assertThat(response.risk().scoreFloorApplied()).isTrue();
        assertThat(response.marketComparables().sampleCount()).isEqualTo(12);
        assertThat(response.marketComparables().median()).isEqualTo(210000000L);
        assertThat(response.marketComparables().bins()).hasSize(1);
    }
}
