package com.project.hugme.domain.diagnosis.controller;

import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisDetailsRequest;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisCreateResponse;
import com.project.hugme.domain.diagnosis.dto.response.RegistryOcrResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import com.project.hugme.domain.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
@Tag(
        name = "전세 위험도 진단",
        description = "전세 위험도 진단 API"
)
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    @Operation(
            summary = "진단 요청 생성",
            description = "사용자 입력을 저장하고 분석 식별자를 생성합니다."
    )
    public ResponseEntity<DiagnosisCreateResponse> createDiagnosis(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @Valid
            @RequestBody
            DiagnosisCreateRequest request
    ) {
        DiagnosisCreateResponse response =
                diagnosisService.createDiagnosis(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{analysisId}/details")
    public ResponseEntity<Void> updateDetails(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId,
            @Valid @RequestBody DiagnosisDetailsRequest request
    ) {
        diagnosisService.updateDetails(userId, analysisId, request);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{analysisId}/analyze")
    @Operation(
            summary = "전세 위험도 분석 실행",
            description = "저장된 진단 및 등기 결과로 AI 분석을 실행합니다."
    )
    public ResponseEntity<FastApiDiagnosisResponse> analyzeDiagnosis(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable
            Long analysisId
    ) {
        return ResponseEntity.ok(
                diagnosisService.analyzeDiagnosis(userId, analysisId)
        );
    }

    @PostMapping(
            value = "/{analysisId}/registry",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "등기부등본 분석",
            description = "등기부등본 PDF를 OCR 분석하고 진단에 연결합니다."
    )
    public ResponseEntity<RegistryOcrResponse> uploadRegistry(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable
            Long analysisId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                diagnosisService.uploadRegistry(userId, analysisId, file)
        );
    }

    @GetMapping("/{analysisId}")
    @Operation(
            summary = "전세 위험도 분석 결과 조회",
            description = "저장된 최종 진단 결과를 조회합니다."
    )
    public ResponseEntity<FastApiDiagnosisResponse> getDiagnosisResult(
            @AuthenticationPrincipal(expression = "userId")
            Long userId,

            @PathVariable
            Long analysisId
    ) {
        return ResponseEntity.ok(
                diagnosisService.getDiagnosisResult(userId, analysisId)
        );
    }
}

