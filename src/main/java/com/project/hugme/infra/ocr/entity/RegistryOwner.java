package com.project.hugme.infra.ocr.entity;

import com.project.hugme.infra.ocr.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * 등본 소유자 1명. 공유자(공동명의)면 같은 registryResult에 여러 행이 붙음.
 */
@Entity
@Table(name = "registry_owners", indexes = {
        @Index(name = "idx_registry_owners_result_id", columnList = "registry_result_id"),
        @Index(name = "idx_registry_owners_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistryOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registry_owner_id")
    private Long registryOwnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_result_id", nullable = false)
    private RegistryResult registryResult;

    @Column(nullable = false, length = 50)
    private String name;

    /** 'YYMMDD-*******' 마스킹 형태 그대로 저장. */
    @Column(name = "jumin_front", nullable = false, length = 20)
    private String juminFront;

    @Column(nullable = false, length = 255)
    private String address;

    /** '1/1', '1/2' 등. */
    @Column(length = 10)
    private String share;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OwnerStatus status = OwnerStatus.CURRENT;

    /** juminFront에서 역산한 나이(오늘 기준, 참고용 - 명단 대조는 게시일 기준으로 별도 계산). */
    private Integer age;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static RegistryOwner create(
            RegistryResult registryResult,
            String name,
            String juminFront,
            String address,
            String share,
            Integer age
    ) {
        RegistryOwner o = new RegistryOwner();
        o.registryResult = registryResult;
        o.name = name;
        o.juminFront = juminFront;
        o.address = address;
        o.share = share;
        o.status = OwnerStatus.CURRENT;
        o.age = age;
        return o;
    }
}
