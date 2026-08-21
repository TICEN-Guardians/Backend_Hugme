package com.project.hugme.domain.diagnosis.controller;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisAddressRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisDetailsRequest;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisCreateResponse;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisReportResponse;
import com.project.hugme.domain.diagnosis.service.DiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/diagnoses")
@RequiredArgsConstructor
public class AnonymousDiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    public ResponseEntity<DiagnosisCreateResponse> createDiagnosis(
            @Valid @RequestBody DiagnosisCreateRequest request
    ) {
        DiagnosisCreateResponse response =
                diagnosisService.createDiagnosis(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{analysisId}/address")
    public ResponseEntity<Void> confirmAddress(
            @PathVariable Long analysisId,
            @RequestHeader("X-Diagnosis-Token") String accessToken,
            @Valid @RequestBody DiagnosisAddressRequest request
    ) {
        diagnosisService.confirmAddress(null, analysisId, accessToken, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{analysisId}/details")
    public ResponseEntity<Void> updateDetails(
            @PathVariable Long analysisId,
            @RequestHeader("X-Diagnosis-Token") String accessToken,
            @Valid @RequestBody DiagnosisDetailsRequest request
    ) {
        diagnosisService.updateDetails(null, analysisId, accessToken, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{analysisId}/analyze")
    public ResponseEntity<FastApiDiagnosisResponse> analyzeDiagnosis(
            @PathVariable Long analysisId,
            @RequestHeader("X-Diagnosis-Token") String accessToken
    ) {
        return ResponseEntity.ok(
                diagnosisService.analyzeDiagnosis(null, analysisId, accessToken)
        );
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<DiagnosisReportResponse> getDiagnosisResult(
            @PathVariable Long analysisId,
            @RequestHeader("X-Diagnosis-Token") String accessToken
    ) {
        return ResponseEntity.ok(
                diagnosisService.getDiagnosisResult(null, analysisId, accessToken)
        );
    }
}
