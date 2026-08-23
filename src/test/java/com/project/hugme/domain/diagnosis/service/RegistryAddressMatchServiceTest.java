package com.project.hugme.domain.diagnosis.service;

import com.project.hugme.domain.diagnosis.enums.RegistryAddressMatchStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistryAddressMatchServiceTest {

    private final RegistryAddressMatchService service =
            new RegistryAddressMatchService();
    private final Map<String, Object> snapshot = Map.of(
            "roadAddress", "경기도 수원시 팔달구 덕영대로 691",
            "jibunAddress", "경기도 수원시 팔달구 화서동 250-4"
    );

    @Test
    void matchesJibunAndRegistryUnitNotation() {
        RegistryAddressMatchStatus status = service.match(
                "경기도 수원시 팔달구 덕영대로 691",
                "107동",
                "1301호",
                snapshot,
                "[집합건물] 경기도 수원시 팔달구 화서동 250-4 화서역 아파트 제107동 제13층 제1301호"
        );
        assertEquals(RegistryAddressMatchStatus.MATCH, status);
    }

    @Test
    void matchesAlphabetDongWithKoreanRegistryNotation() {
        RegistryAddressMatchStatus status = service.match(
                "서울특별시 강서구 화곡동 359-20",
                "B동",
                "203호",
                Map.of("jibunAddress", "서울특별시 강서구 화곡동 359-20"),
                "[집합건물] 서울특별시 강서구 화곡동 359-20 제비동 제2층 제203호"
        );
        assertEquals(RegistryAddressMatchStatus.MATCH, status);
    }

    @Test
    void matchesRoadAndJibunWhenOfficetelNameIsUsedAsDong() {
        RegistryAddressMatchStatus status = service.match(
                "인천광역시 미추홀구 주안동로25번길 47-21",
                "스타캐슬",
                "602호",
                Map.of(
                        "roadAddress", "인천광역시 미추홀구 주안동로25번길 47-21",
                        "jibunAddress", "인천광역시 미추홀구 주안동 75-89"
                ),
                "[집합건물] 인천광역시 미추홀구 주안동 75-89 "
                        + "스타캐슬 제6층 제602호"
        );
        assertEquals(RegistryAddressMatchStatus.MATCH, status);
    }

    @Test
    void rejectsDifferentUnitInSameBuilding() {
        RegistryAddressMatchStatus status = service.match(
                "경기도 수원시 팔달구 덕영대로 691",
                "107동",
                "1302호",
                snapshot,
                "[집합건물] 경기도 수원시 팔달구 화서동 250-4 화서역 아파트 제107동 제13층 제1301호"
        );
        assertEquals(RegistryAddressMatchStatus.MISMATCH, status);
    }

    @Test
    void requiresReviewWhenUnitWasNotRead() {
        RegistryAddressMatchStatus status = service.match(
                "경기도 수원시 팔달구 덕영대로 691",
                "107동",
                "1301호",
                snapshot,
                "[집합건물] 경기도 수원시 팔달구 화서동 250-4 화서역 아파트"
        );
        assertEquals(
                RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED,
                status
        );
    }

    @Test
    void requiresReviewWhenConfirmedUnitWasOmitted() {
        RegistryAddressMatchStatus status = service.match(
                "경기도 수원시 팔달구 덕영대로 691",
                null,
                null,
                snapshot,
                "[집합건물] 경기도 수원시 팔달구 화서동 250-4 화서역 아파트 제107동 제13층 제1301호"
        );
        assertEquals(
                RegistryAddressMatchStatus.PARTIAL_MATCH_REVIEW_REQUIRED,
                status
        );
    }

    @Test
    void rejectsDifferentProperty() {
        RegistryAddressMatchStatus status = service.match(
                "경기도 수원시 팔달구 덕영대로 691",
                null,
                null,
                snapshot,
                "[건물] 서울특별시 관악구 신림동 1604-38"
        );
        assertEquals(RegistryAddressMatchStatus.MISMATCH, status);
    }

    @Test
    void waitsWhenAddressWasNotConfirmed() {
        RegistryAddressMatchStatus status = service.match(
                null,
                null,
                null,
                null,
                "[건물] 서울특별시 관악구 신림동 1604-38"
        );
        assertEquals(
                RegistryAddressMatchStatus.PENDING_ADDRESS_CONFIRMATION,
                status
        );
    }
}
