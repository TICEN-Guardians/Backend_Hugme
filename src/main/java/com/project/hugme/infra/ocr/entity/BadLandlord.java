package com.project.hugme.infra.ocr.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * HUG 상습채무불이행자(악성임대인) 명단 1건.
 * scripts/crawl_bad_landlord.py + load_bad_landlord_to_db.py(파이썬)가 주기적으로
 * 크롤링해서 이 테이블을 통째로 갱신함 (TRUNCATE 후 재적재 - 소명되면 명단에서
 * 빠지는 제도라 부분 갱신보다 전체 교체가 단순하고 안전함).
 *
 * 필드명을 기존 파이썬 적재 스크립트의 INSERT 컬럼명과 동일하게 맞춰서,
 * Spring Boot 기본 네이밍 전략(camelCase -> snake_case)으로 생성되는 컬럼명이
 * 파이썬 쪽 코드 수정 없이 그대로 맞물리게 함.
 */
@Entity
@Table(name = "bad_landlord", indexes = {
        @Index(name = "idx_bad_landlord_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class BadLandlord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /** 법인 임대인은 나이가 없어 null 허용. */
    private Integer age;

    @Column(nullable = false, length = 255)
    private String address;

    /** 원본 주소에서 뽑은 시/군/구 (지금은 소유자 매칭엔 안 쓰지만, 명단 데이터 자체의
     *  정합성/향후 다른 용도를 위해 계속 채워둠). */
    @Column(name = "address_sigungu", length = 50)
    private String addressSigungu;

    /** 임차보증금 반환채무액(원). */
    @Column(name = "return_debt_amount")
    private Long returnDebtAmount;

    /** 채무불이행 경과일수. */
    @Column(name = "default_days")
    private Integer defaultDays;

    /** 강제집행·보전처분 신청 횟수. */
    @Column(name = "enforcement_count")
    private Integer enforcementCount;

    /** 명단 게시일. 이름+나이 매칭 시 이 날짜 기준으로 나이를 역산해 비교함
     *  (오늘 날짜 기준으로 비교하면 어긋남 - 게시 이후 나이를 더 먹었으므로). */
    @Column(name = "posted_date")
    private LocalDate postedDate;

    @CreationTimestamp
    @Column(name = "crawled_at", nullable = false, updatable = false)
    private Instant crawledAt;

    public static BadLandlord create(
            String name,
            Integer age,
            String address,
            String addressSigungu,
            Long returnDebtAmount,
            Integer defaultDays,
            Integer enforcementCount,
            LocalDate postedDate
    ) {
        BadLandlord b = new BadLandlord();
        b.name = name;
        b.age = age;
        b.address = address;
        b.addressSigungu = addressSigungu;
        b.returnDebtAmount = returnDebtAmount;
        b.defaultDays = defaultDays;
        b.enforcementCount = enforcementCount;
        b.postedDate = postedDate;
        return b;
    }
}
