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

    @Column(name = "deposit", nullable = false)
    private Long deposit;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "contract_area", precision = 12, scale = 2)
    private BigDecimal contractArea;

    @Column(name = "landlord_name", length = 100)
    private String landlordName;

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
            String landlordName
    ) {
        Diagnosis diagnosis = new Diagnosis();

        diagnosis.user = user;
        diagnosis.address = address;
        diagnosis.dongName = dongName;
        diagnosis.hoName = hoName;
        diagnosis.deposit = deposit;
        diagnosis.contractDate = contractDate;
        diagnosis.contractArea = contractArea;
        diagnosis.landlordName = landlordName;
        diagnosis.status = DiagnosisStatus.CREATED;

        return diagnosis;
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
