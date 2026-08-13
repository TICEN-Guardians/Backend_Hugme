package com.project.hugme.infra.ai.document.dto;

import java.time.LocalDate;
import java.util.List;

public record DocumentSearchData (

        Long documentId,
        String documentName,
        String documentGroupName,

        String description,
        String issuer,

        String preparationMethod,

        String onlineAvailability,
        String onlineUrl,

        String offlineAvailability,
        String offlineLocation,

        String requiredDocuments,
        String applicantEligibility,

        String fee,
        String processingTime,

        List<String> notes,

        String contactInfo,

        String officialGuideUrl,
        List<String> hugReferenceUrls,

        LocalDate verifiedAt

) {
}