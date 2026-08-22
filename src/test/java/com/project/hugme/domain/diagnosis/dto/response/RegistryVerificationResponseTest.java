package com.project.hugme.domain.diagnosis.dto.response;

import com.project.hugme.domain.diagnosis.entity.Diagnosis;
import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
import com.project.hugme.domain.diagnosis.enums.RegistryAddressMatchStatus;
import com.project.hugme.domain.diagnosis.service.RegistryVerification;
import com.project.hugme.infra.ocr.entity.LandlordWatchlistCheck;
import com.project.hugme.infra.ocr.entity.RegistryOwner;
import com.project.hugme.infra.ocr.entity.RegistryResult;
import com.project.hugme.infra.ocr.entity.RegistryRight;
import com.project.hugme.infra.ocr.enums.CheckStatus;
import com.project.hugme.infra.ocr.enums.MatchStatus;
import com.project.hugme.infra.ocr.enums.MatchType;
import com.project.hugme.infra.ocr.enums.ParseConfidence;
import com.project.hugme.infra.ocr.enums.ParseStatus;
import com.project.hugme.infra.ocr.enums.RightSection;
import com.project.hugme.infra.ocr.enums.RightStatus;
import com.project.hugme.infra.ocr.enums.RightType;
import com.project.hugme.infra.ocr.enums.SectionStatus;
import com.project.hugme.infra.ocr.enums.SourceType;
import com.project.hugme.infra.ocr.enums.TriState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryVerificationResponseTest {

    @Test
    void exposesStoredVerificationFactsAndSourcePages() {
        Diagnosis diagnosis = Diagnosis.create(
                null,
                DiagnosisMode.DETAILED,
                null,
                null
        );
        diagnosis.updateDetails(
                "서울특별시 중구 세종대로 110",
                null,
                "302",
                100_000_000L,
                LocalDate.of(2026, 8, 21),
                null,
                null,
                3,
                "홍길동",
                null,
                true
        );
        RegistryResult result = RegistryResult.create(
                1L,
                ParseStatus.SUCCESS,
                ParseConfidence.HIGH,
                "서울특별시 중구 세종대로 110 제302호",
                LocalDate.of(2026, 8, 21),
                SourceType.PDF_LLM,
                "raw",
                false,
                SectionStatus.EXTRACTED,
                SectionStatus.EXTRACTED,
                TriState.FALSE,
                TriState.FALSE,
                TriState.FALSE,
                TriState.FALSE,
                TriState.FALSE,
                TriState.FALSE,
                TriState.FALSE,
                1,
                81_600_000L
        );
        RegistryOwner owner = RegistryOwner.create(
                result,
                "홍길동",
                "900101",
                "서울특별시 중구",
                "1/1",
                36
        );
        RegistryRight right = RegistryRight.create(
                result,
                RightSection.EUL,
                RightType.MORTGAGE,
                "1",
                "제100호",
                LocalDate.of(2020, 1, 2),
                "한국은행",
                "홍길동",
                81_600_000L,
                RightStatus.ACTIVE,
                "[FILE sample.pdf] [PAGE 4] 근저당권설정 [PAGE 5] 채권최고액"
        );
        LandlordWatchlistCheck check = LandlordWatchlistCheck.create(
                1L,
                result,
                owner,
                CheckStatus.CHECKED,
                MatchStatus.NO_MATCH,
                false,
                null,
                Instant.parse("2026-08-21T00:00:00Z"),
                null
        );

        RegistryVerificationResponse response =
                RegistryVerificationResponse.from(
                        diagnosis,
                        result,
                        List.of(right),
                        List.of(owner),
                        List.of(check),
                        RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED
                );

        assertEquals(LocalDate.of(2026, 8, 21), response.issueDate());
        assertEquals("PARTIAL_MATCH_REVIEW_REQUIRED", response.addressMatchStatus());
        assertTrue(response.addressMatchReviewConfirmed());
        assertEquals("TRUE", response.ownerMatchStatus());
        assertEquals("CHECKED", response.watchlistCheckStatus());
        assertFalse(response.badLandlordMatched());
        assertEquals("홍길동", response.currentOwners().getFirst().name());
        assertEquals("sample.pdf", response.rightEvidence().getFirst()
                .sources().getFirst().fileName());
        assertEquals(
                List.of(4, 5),
                response.rightEvidence().getFirst().sources().stream()
                        .map(RegistryVerificationResponse.SourceReference::page)
                        .toList()
        );

        LandlordWatchlistCheck manualReviewCheck = LandlordWatchlistCheck.create(
                1L,
                result,
                owner,
                CheckStatus.CHECKED,
                MatchStatus.MATCH_NAME_ONLY,
                null,
                MatchType.MANUAL_REVIEW,
                Instant.parse("2026-08-21T00:00:00Z"),
                null
        );
        RegistryVerification.WatchlistSummary manualReview =
                RegistryVerification.summarizeWatchlist(List.of(manualReviewCheck));
        assertEquals("CHECKED", manualReview.status());
        assertNull(manualReview.matched());
    }
}
