package com.project.hugme.domain.checklist.entity.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "document_guides")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentGuide {

    @Id
    @Column(name = "document_id")
    private Long documentId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "preparation_method", columnDefinition = "TEXT")
    private String preparationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_availability", length = 20)
    private AvailabilityStatus onlineAvailability;

    @Column(name = "online_url", length = 1000)
    private String onlineUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "offline_availability", length = 20)
    private AvailabilityStatus offlineAvailability;

    @Column(name = "offline_location", columnDefinition = "TEXT")
    private String offlineLocation;

    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments;

    @Column(name = "applicant_eligibility", columnDefinition = "TEXT")
    private String applicantEligibility;

    @Column(name = "fee", length = 300)
    private String fee;

    @Column(name = "processing_time", length = 300)
    private String processingTime;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "notes", columnDefinition = "TEXT[]")
    private String[] notes;

    @Column(name = "contact_info", length = 500)
    private String contactInfo;

    @Column(name = "official_guide_url", length = 1000)
    private String officialGuideUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "hug_reference_urls", columnDefinition = "TEXT[]")
    private String[] hugReferenceUrls;

    @Column(name = "verified_at")
    private LocalDate verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}