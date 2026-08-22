package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryRight;
import com.project.hugme.infra.ocr.enums.RightStatus;
import com.project.hugme.infra.ocr.enums.RightType;

import java.util.List;

/**
 * 진단 리포트에 함께 노출할 등기부 권리관계 요약.
 * 판정 문구는 화면에서 만들고, 여기서는 사실만 전달한다.
 */
public record RegistrySummaryResponse(
        String parseStatus,
        String parseConfidence,
        List<Mortgage> mortgages,
        Long totalActiveMaxClaimAmount,
        String seizure,
        String provisionalSeizure,
        String provisionalDisposition,
        String auctionCommenced,
        String trustRegistration,
        String jeonseRight,
        String leaseholdRegistration
) {

    public record Mortgage(
            String holder,
            Long amount
    ) {
    }

    public static RegistrySummaryResponse from(
            RegistryResult result,
            List<RegistryRight> rights
    ) {
        if (result == null) {
            return null;
        }

        List<Mortgage> mortgages = rights.stream()
                .filter(right -> right.getStatus() == RightStatus.ACTIVE)
                .filter(right -> right.getRightType() == RightType.MORTGAGE)
                .map(right -> new Mortgage(right.getHolder(), right.getAmount()))
                .toList();

        return new RegistrySummaryResponse(
                name(result.getParseStatus()),
                name(result.getParseConfidence()),
                mortgages,
                result.getTotalActiveMaxClaimAmount(),
                name(result.getSeizure()),
                name(result.getProvisionalSeizure()),
                name(result.getProvisionalDisposition()),
                name(result.getAuctionCommenced()),
                name(result.getTrustRegistration()),
                name(result.getHasActiveJeonseRight()),
                name(result.getHasActiveLeaseholdRegistration())
        );
    }

    private static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
