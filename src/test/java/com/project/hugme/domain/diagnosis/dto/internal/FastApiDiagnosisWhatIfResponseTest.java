package com.project.hugme.domain.diagnosis.dto.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FastApiDiagnosisWhatIfResponseTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    @Test
    void deserializeScenarioResponse() throws Exception {
        String json = """
                {
                  "baseline": {
                    "valuation": {
                      "estimatedSalePrice": 400000000,
                      "estimatedLeasePrice": 180000000
                    },
                    "deposit": 200000000,
                    "activeMaxClaimAmount": 100000000,
                    "indicators": {
                      "leaseToSaleRate": 50.0,
                      "leasePriceGapRate": 11.11,
                      "collateralBurdenAmount": 300000000,
                      "collateralBurdenRate": 75.0,
                      "recoverableAmount": 300000000,
                      "depositShortfall": 0,
                      "remainingCollateralCapacity": 100000000,
                      "priceDropScenarios": {}
                    },
                    "risk": {
                      "score": 49,
                      "baseScore": 49,
                      "grade": "MEDIUM",
                      "breakdown": {
                        "priceBurden": 18,
                        "leaseMarketDeviation": 31,
                        "marketTrend": 0,
                        "policyAdjustment": 0,
                        "rightsAdjustment": 0
                      },
                      "weights": {
                        "priceBurden": 45,
                        "leaseMarketDeviation": 45,
                        "marketTrend": 10,
                        "total": 100
                      },
                      "scoreFloor": null,
                      "floorReasons": [],
                      "scoreFloorApplied": false,
                      "provisionalCollateralBasis": false
                    }
                  },
                  "scenario": {
                    "valuation": {
                      "estimatedSalePrice": 360000000,
                      "estimatedLeasePrice": 180000000
                    },
                    "deposit": 180000000,
                    "activeMaxClaimAmount": 0,
                    "indicators": {
                      "leaseToSaleRate": 50.0,
                      "leasePriceGapRate": 0.0,
                      "collateralBurdenAmount": 180000000,
                      "collateralBurdenRate": 50.0,
                      "recoverableAmount": 360000000,
                      "depositShortfall": 0,
                      "remainingCollateralCapacity": 180000000,
                      "priceDropScenarios": {}
                    },
                    "risk": {
                      "score": 14,
                      "baseScore": 14,
                      "grade": "LOW",
                      "breakdown": {
                        "priceBurden": 0,
                        "leaseMarketDeviation": 14,
                        "marketTrend": 0,
                        "policyAdjustment": 0,
                        "rightsAdjustment": 0
                      },
                      "weights": {
                        "priceBurden": 45,
                        "leaseMarketDeviation": 45,
                        "marketTrend": 10,
                        "total": 100
                      },
                      "scoreFloor": null,
                      "floorReasons": [],
                      "scoreFloorApplied": false,
                      "provisionalCollateralBasis": false
                    }
                  },
                  "scoreChange": -35,
                  "gradeChanged": true,
                  "registryBlockersRemain": false,
                  "unresolvedRiskReasons": [],
                  "depositRecommendation": {
                    "recommendedLimit": 189000000,
                    "currentDeposit": 180000000,
                    "reductionRequired": 0,
                    "withinRecommendedLimit": true,
                    "targetScoreMax": 25,
                    "targetGrade": "LOW",
                    "scoreAtLimit": 14,
                    "calculationBasis": "PRICE_MARKET_AND_REGISTRY",
                    "registryReflected": true,
                    "provisional": false,
                    "adjustmentCanResolveFinalRisk": true,
                    "unresolvedRiskReasons": []
                  }
                }
                """;

        FastApiDiagnosisWhatIfResponse response = objectMapper.readValue(
                json,
                FastApiDiagnosisWhatIfResponse.class
        );

        assertThat(response.baseline().risk().score()).isEqualTo(49);
        assertThat(response.scenario().risk().grade()).isEqualTo("LOW");
        assertThat(response.scoreChange()).isEqualTo(-35);
        assertThat(response.depositRecommendation().recommendedLimit())
                .isEqualTo(189000000L);
    }
}
