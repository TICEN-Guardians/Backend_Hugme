package com.project.hugme.infra.ocr.entity;

import com.project.hugme.infra.ocr.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * 악성임대인(HUG 상습채무불이행자) 명단 대조 결과. 소유자 단위 - 공유자면
 * registryOwner별로 각각 조회되므로 한 analysisId에 여러 행이 붙을 수 있음.
 * registryOwner가 null인 행은 등본 파싱 실패로 소유자 자체를 특정 못 해
 * 조회를 아예 못 한 경우 (checkStatus=NOT_CHECKED).
 */
@Entity
@Table(name = "landlord_watchlist_checks", indexes = {
        @Index(name = "idx_watchlist_checks_analysis_id", columnList = "analysis_id"),
        @Index(name = "idx_watchlist_checks_result_id", columnList = "registry_result_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LandlordWatchlistCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "analysis_id", nullable = false)
    private Long analysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_result_id", nullable = false)
    private RegistryResult registryResult;

    /** null이면 소유자를 못 뽑아 조회 자체를 못 한 경우 (checkStatus=NOT_CHECKED 참고). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_owner_id")
    private RegistryOwner registryOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_status", nullable = false, length = 20)
    private CheckStatus checkStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 20)
    private MatchStatus matchStatus;

    /** true/false/null. 조회 실패나 재확인 필요 상태를 false로 기본값 두지 않기 위해
     *  Boolean 래퍼 타입 사용 (primitive boolean 금지). */
    private Boolean matched;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", length = 20)
    private MatchType matchType;

    @Column(nullable = false, length = 50)
    private String source = "HUG_상습채무불이행자명단";

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    /** match_candidates 원본 (채무액/불이행일수 등) - JSON 문자열로 저장.
     *  Postgres JSONB 컬럼으로 매핑하려면 hypersistence-utils 등 별도 라이브러리 필요 -
     *  우선 String으로 두고 필요시 교체. */
    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static LandlordWatchlistCheck create(
            Long analysisId,
            RegistryResult registryResult,
            RegistryOwner registryOwner,
            CheckStatus checkStatus,
            MatchStatus matchStatus,
            Boolean matched,
            MatchType matchType,
            Instant checkedAt,
            String detailsJson
    ) {
        LandlordWatchlistCheck c = new LandlordWatchlistCheck();
        c.analysisId = analysisId;
        c.registryResult = registryResult;
        c.registryOwner = registryOwner;
        c.checkStatus = checkStatus;
        c.matchStatus = matchStatus;
        c.matched = matched;
        c.matchType = matchType;
        c.source = "HUG_상습채무불이행자명단";
        c.checkedAt = checkedAt;
        c.detailsJson = detailsJson;
        return c;
    }
}
