package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.application.*;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersResponse;
import com.project.hugme.domain.checklist.dto.question.QuestionListResponse;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import com.project.hugme.domain.checklist.service.ApplicationChecklistService;
import com.project.hugme.domain.checklist.service.LeaseContractService;
import com.project.hugme.domain.checklist.service.MyDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
@Tag(
        name = "사용자 맞춤 체크리스트 API",
        description = "OCR과 질문 답변을 통한 사용자 맞춤 체크리스트 API"
)
public class ApplicationChecklistController {

    private final ApplicationChecklistService applicationChecklistService;
    private final MyDocumentService myDocumentService;
    private final LeaseContractService leaseContractService;


    @Operation(
            summary = "1. 맞춤 체크리스트 진입",
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

    @PostMapping("/{applicationId}/lease-contract")
    public ResponseEntity<OCRResponse> uploadLeaseContract(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("applicationId") Long applicationId,
            @RequestParam("file") MultipartFile file
    ) {
        OCRResponse response = leaseContractService.uploadAndAnalyze(
                userId, applicationId, file
        );
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "2. OCR 결과 조회",
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
            summary = "3. OCR 결과 수정 및 확정",
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
            summary = "4. 질문 목록 조회",
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

        //Question단계에 대한 질문들과 그 답변들을 한번에 전달
        QuestionListResponse response =
                applicationChecklistService.getQuestions(
                        userId,
                        applicationId,
                        questionStep
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "5. 질문 답변 제출",
            description = "단계별 질문 답변을 제출하고 추가 질문 또는 최종 서류를 계산합니다."
    )
    @PostMapping("/{applicationId}/answers")
    public ResponseEntity<QuestionAnswersResponse> submitAnswers(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody QuestionAnswersRequest request

    ) {
        QuestionAnswersResponse response =
                applicationChecklistService.submitAnswers(
                        userId,
                        applicationId,
                        request
                );

        return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "6. 현재 준비서류 조회",
            description = "기본·추가·할인 분류별 최종 준비서류를 조회합니다."
    )
    @GetMapping("/{applicationId}/documents")
    public ResponseEntity<MyDocumentListResponse>
    getCurrentDocuments(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable("applicationId")
            Long applicationId
    ) {
        MyDocumentListResponse response =
                myDocumentService
                        .getCurrentDocuments(
                                userId,
                                applicationId
                        );

        return ResponseEntity.ok(response);
    }
}
