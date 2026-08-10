package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.application.ApplicationCreateRequest;
import com.project.hugme.domain.checklist.dto.application.ApplicationCreateResponse;
import com.project.hugme.domain.checklist.service.ApplicationChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
@Tag(
        name = "사용자 맞춤 체크리스트 API",
        description = "OCR과 질문 답변을 통한 사용자 맞춤 체크리스트 API"
)
public class ApplicationChecklistController {

    private final ApplicationChecklistService applicationChecklistService;

    @Operation(
            summary = "맞춤 체크리스트 시작",
            description = "완료된 동일 상품 신청이 있으면 해당 결과를 반환하고, 없으면 새로운 신청을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApplicationCreateResponse> createApplication(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        ApplicationCreateResponse response = applicationChecklistService.createApplication(userId, request);

        return ResponseEntity.ok(response);

    }
}
