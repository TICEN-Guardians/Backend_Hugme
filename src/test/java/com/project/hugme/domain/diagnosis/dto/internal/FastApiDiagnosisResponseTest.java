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
    }
}
