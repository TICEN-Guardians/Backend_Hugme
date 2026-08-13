package com.project.hugme.domain.chatbot.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record DocumentIndexDocument(

        @JsonProperty("document_id")
        Long documentId,

        @JsonProperty("document_name")
        String documentName,

        @JsonProperty("document_group_name")
        String documentGroupName,

        String description,

        String issuer,

        String content,

        @JsonProperty("preparation_method")
        String preparationMethod,

        @JsonProperty("online_availability")
        String onlineAvailability,

        @JsonProperty("online_url")
        String onlineUrl,

        @JsonProperty("offline_availability")
        String offlineAvailability,

        @JsonProperty("offline_location")
        String offlineLocation,

        @JsonProperty("required_documents")
        String requiredDocuments,

        @JsonProperty("applicant_eligibility")
        String applicantEligibility,

        String fee,

        @JsonProperty("processing_time")
        String processingTime,

        List<String> notes,

        @JsonProperty("contact_info")
        String contactInfo,

        @JsonProperty("official_guide_url")
        String officialGuideUrl,

        @JsonProperty("hug_reference_urls")
        List<String> hugReferenceUrls,

        @JsonProperty("verified_at")
        LocalDate verifiedAt,

        float[] embedding
) {
}