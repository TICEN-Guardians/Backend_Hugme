package com.project.hugme.infra.ocr.entity;

import com.project.hugme.infra.ocr.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 등기부등본 OCR/파싱 결과 1건 = row 1개.
 * analysisId로 위험도 분석 건과 연결됨 (Spring 쪽에서 발급한 값을 그대로 저장).
 */
@Entity
@Table(name = "registry_results", indexes = {
        @Index(name = "idx_registry_results_analysis_id", columnList = "analysis_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registry_result_id")
    private Long registryResultId;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 20)
    private ParseStatus parseStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_confidence", nullable = false, length = 10)
    private ParseConfidence parseConfidence;

    @Column(name = "raw_address", length = 255)
    private String rawAddress;

    @Column(name = "dong_name", length = 100)
    private String dongName;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "ho_name", length = 100)
    private String hoName;

    @Column(name = "exclusive_area", precision = 12, scale = 3)
    private BigDecimal exclusiveArea;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "raw_text", columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @Column(name = "has_cancellation_mention", nullable = false)
    private boolean hasCancellationMention = false;

    // ----- 섹션/요약 플래그 -----

    @Enumerated(EnumType.STRING)
    @Column(name = "gap_section_status", nullable = false, length = 20)
    private SectionStatus gapSectionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "eul_section_status", nullable = false, length = 20)
    private SectionStatus eulSectionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "seizure", nullable = false, length = 10)
    private TriState seizure;

    @Enumerated(EnumType.STRING)
    @Column(name = "provisional_seizure", nullable = false, length = 10)
    private TriState provisionalSeizure;

    @Enumerated(EnumType.STRING)
    @Column(name = "provisional_disposition", nullable = false, length = 10)
    private TriState provisionalDisposition;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_commenced", nullable = false, length = 10)
    private TriState auctionCommenced;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_registration", nullable = false, length = 10)
    private TriState trustRegistration;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_active_jeonse_right", nullable = false, length = 10)
    private TriState hasActiveJeonseRight;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_active_leasehold_registration", nullable = false, length = 10)
    private TriState hasActiveLeaseholdRegistration;

    // ----- 파생값 (근저당) - NULL 허용은 의도적: 파싱실패/미확인이면 0이 아니라 NULL -----

    @Column(name = "active_mortgage_count")
    private Integer activeMortgageCount;

    @Column(name = "total_active_max_claim_amount")
    private Long totalActiveMaxClaimAmount;

    @CreationTimestamp
    @Column(name = "parsed_at", nullable = false, updatable = false)
    private Instant parsedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static RegistryResult create(
            Long analysisId,
            ParseStatus parseStatus,
            ParseConfidence parseConfidence,
            String rawAddress,
            LocalDate issueDate,
            SourceType sourceType,
            String rawText,
            boolean hasCancellationMention,
            SectionStatus gapSectionStatus,
            SectionStatus eulSectionStatus,
            TriState seizure,
            TriState provisionalSeizure,
            TriState provisionalDisposition,
            TriState auctionCommenced,
            TriState trustRegistration,
            TriState hasActiveJeonseRight,
            TriState hasActiveLeaseholdRegistration,
            Integer activeMortgageCount,
            Long totalActiveMaxClaimAmount
    ) {
        RegistryResult r = new RegistryResult();
        r.analysisId = analysisId;
        r.parseStatus = parseStatus;
        r.parseConfidence = parseConfidence;
        r.rawAddress = rawAddress;
        r.issueDate = issueDate;
        r.sourceType = sourceType;
        r.rawText = rawText;
        r.hasCancellationMention = hasCancellationMention;
        r.gapSectionStatus = gapSectionStatus;
        r.eulSectionStatus = eulSectionStatus;
        r.seizure = seizure;
        r.provisionalSeizure = provisionalSeizure;
        r.provisionalDisposition = provisionalDisposition;
        r.auctionCommenced = auctionCommenced;
        r.trustRegistration = trustRegistration;
        r.hasActiveJeonseRight = hasActiveJeonseRight;
        r.hasActiveLeaseholdRegistration = hasActiveLeaseholdRegistration;
        r.activeMortgageCount = activeMortgageCount;
        r.totalActiveMaxClaimAmount = totalActiveMaxClaimAmount;
        return r;
    }
}
