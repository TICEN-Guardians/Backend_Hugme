package com.project.hugme.domain.diagnosis.entity;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "diagnosis_results")
@NoArgsConstructor(access = PROTECTED)
public class DiagnosisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_result_id")
    private Long diagnosisResultId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false, unique = true)
    private Diagnosis diagnosis;

    @Column(name = "estimated_sale_price", nullable = false)
    private Long estimatedSalePrice;

    @Column(name = "estimated_lease_price", nullable = false)
    private Long estimatedLeasePrice;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "risk_grade", nullable = false, length = 20)
    private String riskGrade;

    @Column(name = "valuation_reliability", nullable = false, length = 20)
    private String valuationReliability;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @Column(name = "response_json", nullable = false, columnDefinition = "TEXT")
    private String responseJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static DiagnosisResult create(
            Diagnosis diagnosis,
            FastApiDiagnosisResponse response,
            String responseJson
    ) {
        DiagnosisResult result = new DiagnosisResult();
        result.diagnosis = diagnosis;
        result.update(response, responseJson);
        return result;
    }

    public void update(
            FastApiDiagnosisResponse response,
            String responseJson
    ) {
        estimatedSalePrice = response.valuation().estimatedSalePrice();
        estimatedLeasePrice = response.valuation().estimatedLeasePrice();
        riskScore = response.risk().score();
        riskGrade = response.risk().grade();
        valuationReliability = response.valuationReliability();
        analyzedAt = response.analyzedAt();
        this.responseJson = responseJson;
    }
}
