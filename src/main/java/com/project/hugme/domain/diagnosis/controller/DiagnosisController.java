package com.project.hugme.domain.diagnosis.controller;

import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisResponse;
import com.project.hugme.domain.diagnosis.dto.internal.FastApiDiagnosisWhatIfResponse;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisAddressRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisCreateRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisDetailsRequest;
import com.project.hugme.domain.diagnosis.dto.request.DiagnosisWhatIfRequest;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisCreateResponse;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisListResponse;
import com.project.hugme.domain.diagnosis.dto.response.DiagnosisReportResponse;
import com.project.hugme.domain.diagnosis.dto.response.RegistryOcrResponse;
import com.project.hugme.domain.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
@Tag(name = "전세 위험도 진단", description = "로그인 사용자 진단 API")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    @Operation(summary = "진단 생성")
    public ResponseEntity<DiagnosisCreateResponse> createDiagnosis(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody DiagnosisCreateRequest request
    ) {
        DiagnosisCreateResponse response =
                diagnosisService.createDiagnosis(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{analysisId}/address")
    public ResponseEntity<Void> confirmAddress(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId,
            @Valid @RequestBody DiagnosisAddressRequest request
    ) {
        diagnosisService.confirmAddress(userId, analysisId, null, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{analysisId}/details")
    public ResponseEntity<Void> updateDetails(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId,
            @Valid @RequestBody DiagnosisDetailsRequest request
    ) {
        diagnosisService.updateDetails(userId, analysisId, null, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{analysisId}/registry")
    @Operation(summary = "등기부등본 분석")
    public ResponseEntity<RegistryOcrResponse> uploadRegistry(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                diagnosisService.uploadRegistry(userId, analysisId, files)
        );
    }

    @PostMapping("/{analysisId}/analyze")
    @Operation(summary = "전세 위험도 분석 실행")
    public ResponseEntity<FastApiDiagnosisResponse> analyzeDiagnosis(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId
    ) {
        return ResponseEntity.ok(
                diagnosisService.analyzeDiagnosis(userId, analysisId, null)
        );
    }

    @PostMapping("/{analysisId}/scenarios")
    @Operation(summary = "전세 위험도 What-if 계산")
    public ResponseEntity<FastApiDiagnosisWhatIfResponse> calculateWhatIf(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId,
            @Valid @RequestBody DiagnosisWhatIfRequest request
    ) {
        return ResponseEntity.ok(
                diagnosisService.calculateWhatIf(
                        userId,
                        analysisId,
                        null,
                        request
                )
        );
    }

    @GetMapping("/{analysisId}")
    @Operation(summary = "전세 위험도 분석 결과 조회")
    public ResponseEntity<DiagnosisReportResponse> getDiagnosisResult(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @PathVariable Long analysisId
    ) {
        return ResponseEntity.ok(
                diagnosisService.getDiagnosisResult(userId, analysisId, null)
        );
    }

    @GetMapping("/completed")
    @Operation(
            summary = "저장된 진단 목록 조회",
            description = "로그인 사용자의 완료된 전세 위험도 진단을 최근 업데이트 순으로 조회합니다."
    )
    public ResponseEntity<List<DiagnosisListResponse>>
    getCompletedDiagnoses(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "userId")
            Long userId
    ) {
        List<DiagnosisListResponse> response =
                diagnosisService
                        .getCompletedDiagnoses(
                                userId
                        );

        return ResponseEntity.ok(response);
    }
}
