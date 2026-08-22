package com.project.hugme.domain.notification.controller;

import com.project.hugme.domain.notification.dto.NotificationAuthorizationResponse;
import com.project.hugme.domain.notification.dto.NotificationSendRequest;
import com.project.hugme.domain.notification.dto.NotificationSendResponse;
import com.project.hugme.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/api/applications/{applicationId}/notifications"
)
@Tag(
        name = "카카오톡 알림 API",
        description = "전세보증금 반환보증 신청기한을 내 카카오톡으로 전송합니다."
)
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "1. 카카오 인증 URL 생성",
            description = """
                    카카오톡 나에게 보내기 권한을 얻기 위한
                    카카오 인증 URL을 생성합니다.
                    """
    )
    @PostMapping("/kakao/authorize")
    public ResponseEntity<NotificationAuthorizationResponse>
    createKakaoAuthorizationUrl(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable("applicationId")
            Long applicationId
    ) {
        NotificationAuthorizationResponse response =
                notificationService.createKakaoAuthorizationUrl(
                        userId,
                        applicationId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "2. 카카오톡으로 신청기한 전송",
            description = """
                    카카오 인증 후 발급받은 인가 코드로
                    액세스 토큰을 발급하고 신청기한 D-day를
                    사용자의 나와의 채팅으로 전송합니다.
                    """
    )
    @PostMapping("/kakao/me")
    public ResponseEntity<NotificationSendResponse>
    sendKakaoMessage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable("applicationId")
            Long applicationId,

            @Valid @RequestBody
            NotificationSendRequest request
    ) {
        NotificationSendResponse response =
                notificationService.sendKakaoMessage(
                        userId,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}