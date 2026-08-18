package com.project.hugme.domain.diagnosis.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.hugme.domain.diagnosis.dto.response.RegistryOcrResponse;
import java.math.BigDecimal;
import java.util.List;

public record FastApiRegistryResponse(
        @JsonProperty("owner_info") OwnerInfo ownerInfo,
        List<Result> results,
        @JsonProperty("overall_match_status") String overallMatchStatus
) {
    public record OwnerInfo(
            @JsonProperty("parse_status") String parseStatus,
            @JsonProperty("parse_confidence") String parseConfidence,
            @JsonProperty("property_address") String propertyAddress,
            @JsonProperty("dong_name") String dongName,
            Integer floor,
            @JsonProperty("ho_name") String hoName,
            @JsonProperty("exclusive_area") BigDecimal exclusiveArea,
            @JsonProperty("current_owners") List<Owner> currentOwners
    ) {}
    public record Owner(String name, String share) {}
    public record Result(@JsonProperty("match_status") String matchStatus) {}

    public RegistryOcrResponse toResponse() {
        List<RegistryOcrResponse.Owner> owners = ownerInfo.currentOwners().stream()
                .map(owner -> new RegistryOcrResponse.Owner(owner.name(), owner.share()))
                .toList();
        return new RegistryOcrResponse(ownerInfo.parseStatus(), ownerInfo.parseConfidence(),
                ownerInfo.propertyAddress(), ownerInfo.dongName(), ownerInfo.floor(),
                ownerInfo.hoName(), ownerInfo.exclusiveArea(), owners, overallMatchStatus);
    }
}
