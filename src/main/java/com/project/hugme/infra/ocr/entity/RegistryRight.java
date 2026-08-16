package com.project.hugme.infra.ocr.entity;

import com.project.hugme.infra.ocr.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 갑구/을구 권리 항목 원본, 순위번호 단위 (통합 테이블).
 * 근저당/전세권/임차권등기/압류 등을 rightType으로 구분해서 한 테이블에 담음
 * (mortgages/jeonseRights/leaseholdRegistrations를 따로 안 만드는 이유:
 *  같은 데이터를 종류만 다르게 중복 저장하는 걸 피하기 위함 - API 응답에서만
 *  rightType 기준으로 분류해서 보여줌).
 */
@Entity
@Table(name = "registry_rights", indexes = {
        @Index(name = "idx_registry_rights_result_id", columnList = "registry_result_id"),
        @Index(name = "idx_registry_rights_type", columnList = "registry_result_id, right_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistryRight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "right_id")
    private Long rightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_result_id", nullable = false)
    private RegistryResult registryResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RightSection section;

    @Enumerated(EnumType.STRING)
    @Column(name = "right_type", nullable = false, length = 30)
    private RightType rightType;

    /** 부기등기는 '1-1' 형태. */
    @Column(name = "rank_no", nullable = false, length = 10)
    private String rankNo;

    @Column(name = "receipt_no", length = 30)
    private String receiptNo;

    @Column(name = "registered_at")
    private LocalDate registeredAt;

    /** 근저당권자 / 전세권자 / 임차권자 / 채권자 등. */
    @Column(length = 100)
    private String holder;

    @Column(length = 50)
    private String debtor;

    /** 채권최고액 / 전세금 / 임차보증금 / 청구금액. */
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RightStatus status;

    @Column(name = "raw_text", columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static RegistryRight create(
            RegistryResult registryResult,
            RightSection section,
            RightType rightType,
            String rankNo,
            String receiptNo,
            LocalDate registeredAt,
            String holder,
            String debtor,
            Long amount,
            RightStatus status,
            String rawText
    ) {
        RegistryRight r = new RegistryRight();
        r.registryResult = registryResult;
        r.section = section;
        r.rightType = rightType;
        r.rankNo = rankNo;
        r.receiptNo = receiptNo;
        r.registeredAt = registeredAt;
        r.holder = holder;
        r.debtor = debtor;
        r.amount = amount;
        r.status = status;
        r.rawText = rawText;
        return r;
    }
}
