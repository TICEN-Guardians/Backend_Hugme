package com.project.hugme.domain.diagnosis.entity;

import com.project.hugme.domain.diagnosis.enums.DiagnosisStatus;
import com.project.hugme.domain.diagnosis.enums.HousingType;
import com.project.hugme.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
        name = "diagnoses",
        indexes = {
                @Index(
                        name = "idx_diagnoses_user_id",
                        columnList = "user_id"
                )
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "normalized_address", length = 500)
    private String normalizedAddress;

    @Column(name = "dong_name", length = 100)
    private String dongName;

    @Column(name = "ho_name", length = 100)
    private String hoName;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type", length = 30)
    private HousingType housingType;

    @Column(name = "deposit")
    private Long deposit;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "contract_area", precision = 12, scale = 2)
    private BigDecimal contractArea;

    @Column(name = "exclusive_area", precision = 12, scale = 2)
    private BigDecimal exclusiveArea;

    @Column(name = "target_floor")
    private Integer floor;

    @Column(name = "landlord_name", length = 100)
    private String landlordName;

    /**
     * properties/resolve 로 확보한 주소·건축물대장 정보(JSON 문자열).
     * 분석 단계에서 그대로 AI에 넘겨 공공 API 재조회를 건너뛰게 한다.
     * 성능을 위한 부가 정보라 값이 없어도 진단은 정상 동작한다.
     */
    @Column(name = "property_snapshot", columnDefinition = "TEXT")
    private String propertySnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DiagnosisStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Diagnosis create(
            User user,
            String address,
            String dongName,
            String hoName,
            Long deposit,
            LocalDate contractDate,
            BigDecimal contractArea,
            BigDecimal exclusiveArea,
            Integer floor,
            String landlordName,
            String propertySnapshot
    ) {
        Diagnosis diagnosis = new Diagnosis();

        diagnosis.user = user;
        diagnosis.address = address;
        diagnosis.dongName = dongName;
        diagnosis.hoName = hoName;
        diagnosis.deposit = deposit;
        diagnosis.contractDate = contractDate;
        diagnosis.contractArea = contractArea;
        diagnosis.exclusiveArea = exclusiveArea;
        diagnosis.floor = floor;
        diagnosis.landlordName = landlordName;
        diagnosis.propertySnapshot = propertySnapshot;
        diagnosis.status = DiagnosisStatus.CREATED;

        return diagnosis;
    }

    public void updateDetails(
            String dongName,
            String hoName,
            Long deposit,
            LocalDate contractDate,
            BigDecimal contractArea,
            BigDecimal exclusiveArea,
            Integer floor,
            String landlordName
    ) {
        this.dongName = dongName;
        this.hoName = hoName;
        this.deposit = deposit;
        this.contractDate = contractDate;
        this.contractArea = contractArea;
        this.exclusiveArea = exclusiveArea;
        this.floor = floor;
        this.landlordName = landlordName;
    }
    public void markAnalyzing() {
        status = DiagnosisStatus.ANALYZING;
    }

    public void complete(String normalizedAddress, HousingType housingType) {
        this.normalizedAddress = normalizedAddress;
        this.housingType = housingType;
        status = DiagnosisStatus.COMPLETED;
    }
}
