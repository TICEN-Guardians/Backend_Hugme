package com.project.hugme.domain.diagnosis.controller;

import com.project.hugme.domain.diagnosis.dto.request.PropertyResolveRequest;
import com.project.hugme.domain.diagnosis.dto.response.PropertyResolveResponse;
import com.project.hugme.domain.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Tag(
        name = "전세 위험도 진단",
        description = "진단 대상 주택 확인 API"
)
public class PropertyResolveController {

    private final DiagnosisService diagnosisService;

    @PostMapping("/resolve")
    @Operation(
            summary = "진단 대상 주소 확인",
            description = "주소를 표준화하고 주택유형을 확인합니다."
    )
    public ResponseEntity<PropertyResolveResponse> resolveProperty(
            @Valid @RequestBody PropertyResolveRequest request
    ) {
        PropertyResolveResponse response =
                diagnosisService.resolveProperty(request);

        return ResponseEntity.ok(response);
    }
}