package com.project.hugme.domain.diagnosis.controller;

import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisCreateResponse;
import com.project.hugme.domain.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}