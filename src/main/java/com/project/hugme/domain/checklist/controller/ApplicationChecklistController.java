package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.application.ApplicationCreateRequest;
import com.project.hugme.domain.checklist.dto.application.ApplicationCreateResponse;
import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import com.project.hugme.domain.checklist.dto.application.OCRUpdateRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionListResponse;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import com.project.hugme.domain.checklist.service.ApplicationChecklistService;
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


    @Operation(
            summary = "OCR 결과 조회",
            description = "로그인 사용자의 applicationId에 저장된 임대차계약서 OCR 분석 결과를 조회합니다."
    )
    @GetMapping("/{applicationId}/info")
    public ResponseEntity<OCRResponse> getOCRResult(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("applicationId") Long applicationId
    ) {
        OCRResponse response = applicationChecklistService.getOCRResult(userId, applicationId);

        return ResponseEntity.ok(response);
    }
    // userid 랑 application의 applicationId

    @Operation(
            summary = "OCR 결과 수정 및 확정",
            description = "임대차계약서 OCR 분석 결과를 사용자가 수정하고 최종 확정합니다."
    )
    @PatchMapping("/{applicationId}/info")
    public ResponseEntity<OCRResponse> updateOCRResult(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("applicationId") Long applicationId,

            @Valid @RequestBody OCRUpdateRequest request


    ) {
        OCRResponse response = applicationChecklistService.updateOCRResult(
                userId,
                applicationId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "질문 목록 조회",
            description = "신청 상품과 OCR 결과에 맞는 단계별 질문을 조회합니다."
    )
    @GetMapping("/{applicationId}/questions")
    public ResponseEntity<QuestionListResponse> getQuestions(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("applicationId") Long applicationId,
            @Parameter(
                    description = "질문 단계",
                    required = true,
                    example = "STEP1"
            )
            @RequestParam("step") QuestionStep questionStep) {

        QuestionListResponse response =
                applicationChecklistService.getQuestions(
                        userId,
                        applicationId,
                        questionStep
                );

        return ResponseEntity.ok(response);
    }
}
