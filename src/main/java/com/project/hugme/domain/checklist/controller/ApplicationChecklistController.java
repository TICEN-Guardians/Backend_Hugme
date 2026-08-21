package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.application.*;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersResponse;
import com.project.hugme.domain.checklist.dto.question.QuestionListResponse;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import com.project.hugme.domain.checklist.service.ApplicationChecklistService;
import com.project.hugme.domain.checklist.service.LeaseContractService;
import com.project.hugme.domain.checklist.service.MyDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
@Tag(
        name = "일반 사용자 맞춤 체크리스트 API",
        description = "OCR과 질문 답변을 통한 로그인 사용자 맞춤 체크리스트 API"
)
public class ApplicationChecklistController {

    private final ApplicationChecklistService applicationChecklistService;
    private final MyDocumentService myDocumentService;
    private final LeaseContractService leaseContractService;


    @Operation(
            summary = "1. 맞춤 체크리스트 진입",
            description = "새로운 신청을 생성합니다."
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
            summary = "2. 기존 체크리스트 진입",
            description = "이전 내역이 있으면 그 내용을 불러옵니다."
    )
    @GetMapping("/current")
    public ResponseEntity<ApplicationCreateResponse> getCurrentApplication(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId") Long userId,

            @RequestParam("productCode") ProductCode productCode
    ) {
        ApplicationCreateResponse response =
                applicationChecklistService
                        .getCurrentApplication(
                                userId,
                                productCode
                        );

        return ResponseEntity.ok(response);
    }



    @Operation(
            summary = "3. 임대차계약서 업로드 및 OCR 분석",
            description = "임대차계약서 파일을 업로드하고 OCR 분석을 수행합니다."
    )
    @PostMapping(
            value = "/{applicationId}/lease-contract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
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
            summary = "4. OCR 결과 조회",
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
            summary = "5. OCR 결과 수정 및 확정",
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
            summary = "6. 질문 목록 조회",
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
            summary = "7. 질문 답변 제출",
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
            summary = "8. 최종 준비서류 조회",
            description = "기본·추가·할인 분류별 최종 준비서류를 조회합니다."
    )
    @GetMapping("/{applicationId}/result-documents")
    public ResponseEntity<ResultDocumentListResponse>
    getResultDocuments(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable("applicationId")
            Long applicationId
    ) {
        ResultDocumentListResponse response =
                myDocumentService
                        .getResultDocuments(
                                userId,
                                applicationId
                        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "9. 최종 준비서류 유무 확인",
            description = """
                productCode가 있으면 완료된 신청의 상품과 요청 상품이 같은지도 확인합니다.
                """
    )
    @GetMapping("/check")
    public ResponseEntity<Boolean>
    getResultDocumentsCheck(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @RequestParam(
                    name = "productCode"
            )
            ProductCode productCode
    ) {
        boolean exists =
                myDocumentService.hasResultDocuments(
                        userId, productCode
                );

        return ResponseEntity.ok(exists);
    }


//    @Operation(
//            summary = "8. 현재 준비서류 조회",
//            description = "기본·추가·할인 분류별 최종 준비서류를 조회합니다."
//    )
//    @GetMapping("/{applicationId}/documents")
//    public ResponseEntity<MyDocumentListResponse>
//    getCurrentDocuments(
//            @AuthenticationPrincipal(expression = "userId")
//            Long userId,
//
//            @PathVariable("applicationId")
//            Long applicationId
//    ) {
//        MyDocumentListResponse response =
//                myDocumentService
//                        .getCurrentDocuments(
//                                userId,
//                                applicationId
//                        );
//
//        return ResponseEntity.ok(response);
//    }


}
