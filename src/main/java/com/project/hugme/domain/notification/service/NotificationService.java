package com.project.hugme.domain.notification.service;

import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.repository.ApplicationInfoRepository;
import com.project.hugme.domain.notification.client.KakaoMessageClient;
import com.project.hugme.domain.notification.client.KakaoOAuthClient;
import com.project.hugme.domain.notification.config.KakaoMessageProperties;
import com.project.hugme.domain.notification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationInfoRepository applicationInfoRepository;

    private final KakaoMessageProperties kakaoMessageProperties;
    private final KakaoStateStore kakaoStateStore;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoMessageClient kakaoMessageClient;
    private final DDayCalculator dDayCalculator;

    /*
     * 카카오 인증 URL 생성
     */
    public NotificationAuthorizationResponse
    createKakaoAuthorizationUrl(
            Long userId,
            Long applicationId
    ) {
        /*
         * applicationId가 로그인 사용자의 신청인지 확인한다.
         */
        applicationInfoRepository
                .findByApplicationIdAndUserId(
                        applicationId,
                        userId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "신청정보를 찾을 수 없습니다."
                        )
                );

        /*
         * userId와 applicationId를 연결한
         * 일회용 state 값을 생성한다.
         */
        String state =
                kakaoStateStore.create(
                        userId,
                        applicationId
                );

        /*
         * 카카오 인가 코드 요청 주소를 생성한다.
         */
        String authorizationUrl =
                UriComponentsBuilder
                        .fromUriString(
                                "https://kauth.kakao.com/oauth/authorize"
                        )
                        .queryParam(
                                "client_id",
                                kakaoMessageProperties.getClientId()
                        )
                        .queryParam(
                                "redirect_uri",
                                kakaoMessageProperties.getRedirectUri()
                        )
                        .queryParam(
                                "response_type",
                                "code"
                        )
                        .queryParam(
                                "scope",
                                "talk_message"
                        )
                        .queryParam(
                                "state",
                                state
                        )
                        .build()
                        .encode()
                        .toUriString();

        return new NotificationAuthorizationResponse(
                authorizationUrl
        );
    }

    /*
     * D-day를 계산하여 내 카카오톡으로 전송
     */
    public NotificationSendResponse sendKakaoMessage(
            Long userId,
            Long applicationId,
            NotificationSendRequest request
    ) {
        ApplicationInfo applicationInfo =
                applicationInfoRepository
                        .findByApplicationIdAndUserId(
                                applicationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "신청정보를 찾을 수 없습니다."
                                )
                        );

        /*
         * 신청한 보증상품 코드를 가져온다.
         */
        ProductCode productCode =
                applicationInfo
                        .getApplication()
                        .getProduct()
                        .getProductCode();

        DDayResult dDayResult =
                dDayCalculator.calculate(
                        applicationInfo.getContractType(),
                        request.contractStartDate(),
                        request.contractEndDate(),
                        request.balancePaymentDate(),
                        request.moveInDate()
                );

        kakaoStateStore.validateAndConsume(
                request.state(),
                userId,
                applicationId
        );

        KakaoTokenResponse tokenResponse =
                kakaoOAuthClient.issueToken(
                        request.code()
                );

        /*
         * 상품명과 D-day를 사용해 메시지를 생성한다.
         */
        String message =
                createMessage(
                        productCode,
                        dDayResult.dDay()
                );

        kakaoMessageClient.sendToMe(
                tokenResponse.accessToken(),
                message
        );

        return new NotificationSendResponse(
                applicationId,
                dDayResult.applicationDeadline(),
                dDayResult.dDay(),
                true
        );
    }

    private String createMessage(
            ProductCode productCode,
            long dDay
    ) {
        String productName;

        if (productCode == ProductCode.GENERAL) {
            productName =
                    "전세보증금 반환보증";

        } else if (
                productCode == ProductCode.SPECIAL
        ) {
            productName =
                    "특례반환보증";

        } else {
            throw new IllegalArgumentException(
                    "카카오 알림을 지원하지 않는 보증상품입니다."
            );
        }

        if (dDay > 0) {
            return """
                [HUGME]

                %s 신청기한까지
                D-%d일 남았습니다.
                """.formatted(
                    productName,
                    dDay
            );
        }

        if (dDay == 0) {
            return """
                [HUGME]

                오늘은 %s
                신청기한 마지막 날입니다.
                """.formatted(
                    productName
            );
        }

        return """
            [HUGME]

            %s의 일반 신청기한이
            %d일 지났습니다.

            정확한 가입 가능 여부는 HUG에 확인해 주세요.
            """.formatted(
                productName,
                Math.abs(dDay)
        );
    }
}