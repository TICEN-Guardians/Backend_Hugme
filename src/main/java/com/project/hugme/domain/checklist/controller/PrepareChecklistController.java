package com.project.hugme.domain.checklist.controller;

import lombok.RequiredArgsConstructor;
import com.project.hugme.domain.checklist.dto.application.*;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersResponse;
import com.project.hugme.domain.checklist.dto.question.QuestionListResponse;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import com.project.hugme.domain.checklist.service.ApplicationChecklistService;
import com.project.hugme.domain.checklist.service.LeaseContractService;
import com.project.hugme.domain.checklist.service.MyDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications/prepare")
@Tag(
        name = "모의테스트 체크리스트 API",
        description = "로그인과 OCR 없이 진행하는 모의테스트 체크리스트 API"
)
public class PrepareChecklistController {

    private final ApplicationChecklistService applicationChecklistService;
    private final MyDocumentService myDocumentService;
    private final LeaseContractService leaseContractService;


    @SecurityRequirements
    @Operation(
            summary = "1. 모의테스트 체크리스트 진입",
            description = "기본 신청정보로 READY 상태의 모의테스트 신청을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApplicationCreateResponse> prepareApplication(
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        ApplicationCreateResponse response =
                applicationChecklistService
                        .prepareApplication(
                                0L,
                                request
                        );


        return ResponseEntity.ok(response);

    }
    @SecurityRequirements
    @Operation(
            summary = "2. 모의테스트 신청정보 조회",
            description = "모의테스트 신청에 저장된 주택유형, 계약유형, 임차인·임대인 유형 등의 정보를 조회합니다."
    )
    @GetMapping("/{applicationId}/info")
    public ResponseEntity<OCRResponse> getOCRResult(
            @PathVariable("applicationId") Long applicationId
    ) {
        OCRResponse response = applicationChecklistService.getOCRResult((long)0, applicationId);

        return ResponseEntity.ok(response);
    }
    // userid 랑 application의 applicationId

    @SecurityRequirements
    @Operation(
            summary = "3. 모의테스트 신청정보 수정 및 확정",
            description = "모의테스트에 사용할 주택유형, 계약유형, 임차인·임대인 유형 등의 신청정보를 수정하고 확정합니다."
    )
    @PatchMapping("/{applicationId}/info")
    public ResponseEntity<OCRResponse> updateOCRResult(
            @PathVariable("applicationId") Long applicationId,
            @Valid @RequestBody OCRUpdateRequest request


    ) {
        OCRResponse response = applicationChecklistService.updateOCRResult(
                (long)0,
                applicationId,
                request
        );

        return ResponseEntity.ok(response);
    }
    @SecurityRequirements
    @Operation(
            summary = "4. 모의테스트 질문 목록 조회",
            description = "모의테스트 신청의 단계별 질문과 선택지를 조회합니다."
    )
    @GetMapping("/{applicationId}/questions")
    public ResponseEntity<QuestionListResponse>
    getPrepareQuestions(
            @PathVariable("applicationId")
            Long applicationId,

            @RequestParam("step")
            QuestionStep questionStep
    ) {
        QuestionListResponse response =
                applicationChecklistService.getQuestions(
                        0L,
                        applicationId,
                        questionStep
                );

        return ResponseEntity.ok(response);
    }
    @SecurityRequirements
    @Operation(
            summary = "5. 모의테스트 질문 답변 제출",
            description = "모의테스트 질문의 답변을 제출하고 다음 질문 또는 최종 결과를 계산합니다."
    )
    @PostMapping("/{applicationId}/answers")
    public ResponseEntity<QuestionAnswersResponse>
    submitPrepareAnswers(
            @PathVariable("applicationId")
            Long applicationId,

            @Valid
            @RequestBody
            QuestionAnswersRequest request
    ) {
        QuestionAnswersResponse response =
                applicationChecklistService
                        .submitAnswers(
                                0L,
                                applicationId,
                                request
                        );

        return ResponseEntity.ok(response);
    }
    @SecurityRequirements
    @Operation(
            summary = "6. 모의테스트 최종 준비서류 조회",
            description = "모의테스트 질문 결과로 계산된 최종 준비서류를 조회합니다."
    )
    @GetMapping("/{applicationId}/result-documents")
    public ResponseEntity<ResultDocumentListResponse>
    getPrepareResultDocuments(
            @PathVariable("applicationId")
            Long applicationId
    ) {
        ResultDocumentListResponse response =
                myDocumentService
                        .getResultDocuments(
                                0L,
                                applicationId
                        );

        return ResponseEntity.ok(response);
    }


}