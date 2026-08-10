package com.project.hugme.domain.checklist.entity.application;

import com.project.hugme.domain.checklist.entity.product.HousingType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "application_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationInfo {

    @Id
    @Column(name = "application_id")
    private Long applicationId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false
    )
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_type_id")
    private HousingType housingType;

    @Column(
            name = "contract_address",
            length = 500
    )
    private String contractAddress;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "contract_type",
            length = 20
    )
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tenant_type",
            length = 20
    )
    private PartyType tenantType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "landlord_type",
            length = 20
    )
    private PartyType landlordType;

    @Column(name = "fixed_date_confirmed")
    private Boolean fixedDateConfirmed;

    @Column(name = "officetel_residential_marked")
    private Boolean officetelResidentialMarked;

    @Column(name = "landlord_proxy_contract")
    private Boolean landlordProxyContract;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;
}