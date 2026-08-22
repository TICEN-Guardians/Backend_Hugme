package com.project.hugme.domain.diagnosis.entity;

import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
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
@Table(name = "diagnoses", indexes = {
        @Index(name = "idx_diagnoses_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = PROTECTED)
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "diagnosis_mode", nullable = false, length = 20)
    private DiagnosisMode mode;

    @Column(name = "anonymous_access_token_hash", length = 64)
    private String anonymousAccessTokenHash;

    @Column(name = "anonymous_access_expires_at")
    private Instant anonymousAccessExpiresAt;

    @Column(name = "address", length = 500)
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
            DiagnosisMode mode,
            String anonymousAccessTokenHash,
            Instant anonymousAccessExpiresAt
    ) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.user = user;
        diagnosis.mode = mode;
        diagnosis.anonymousAccessTokenHash = anonymousAccessTokenHash;
        diagnosis.anonymousAccessExpiresAt = anonymousAccessExpiresAt;
        diagnosis.status = DiagnosisStatus.CREATED;
        return diagnosis;
    }

    public void updateAddress(
            String address,
            String dongName,
            String hoName,
            String propertySnapshot
    ) {
        this.address = address;
        this.dongName = dongName;
        this.hoName = hoName;
        this.propertySnapshot = propertySnapshot;
    }

    public void markAddressConfirmed(boolean registryReady) {
        if (hasDetails()) {
            markDetailsReady(registryReady);
            return;
        }
        status = mode == DiagnosisMode.DETAILED && registryReady
                ? DiagnosisStatus.REGISTRY_ANALYZED
                : DiagnosisStatus.CREATED;
    }

    public void updateDetails(
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
        this.address = address;
        this.dongName = dongName;
        this.hoName = hoName;
        this.deposit = deposit;
        this.contractDate = contractDate;
        this.contractArea = contractArea;
        this.exclusiveArea = exclusiveArea;
        this.floor = floor;
        this.landlordName = landlordName;
        this.propertySnapshot = propertySnapshot;
    }

    public void markDetailsReady(boolean registryReady) {
        status = mode == DiagnosisMode.QUICK || registryReady
                ? DiagnosisStatus.READY
                : DiagnosisStatus.DETAILS_READY;
    }

    public void markRegistryProcessed(boolean successful) {
        if (!successful) {
            status = hasDetails()
                    ? DiagnosisStatus.DETAILS_READY
                    : DiagnosisStatus.CREATED;
            return;
        }
        status = hasDetails()
                ? DiagnosisStatus.READY
                : DiagnosisStatus.REGISTRY_ANALYZED;
    }

    public boolean hasDetails() {
        return address != null && !address.isBlank()
                && deposit != null
                && contractDate != null
                && (contractArea != null || exclusiveArea != null);
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
