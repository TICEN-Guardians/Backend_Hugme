package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.product.ProductChecklistResponse;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.service.ProductChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(
        name = "상품별 준비서류 API",
        description = "로그인 없이 보증상품별 전체 체크리스트와 제출서류를 조회하는 API"
)
public class ProductChecklistController {

    private final ProductChecklistService productChecklistService;

    @Operation(
            summary = "상품별 전체 준비서류 조회",
            description = "상품 코드에 해당하는 기본서류, 추가서류, 보증료 할인서류를 " +
                    "체크리스트 항목과 실제 제출서류까지 함께 조회합니다."
    )
    @GetMapping("/{productCode}/checklist")
    public ResponseEntity<ProductChecklistResponse> getProductChecklist(
            @Parameter(
                    description = "보증상품 코드",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"GENERAL", "SPECIAL"},
                            example = "GENERAL"
                    )
            )
            @PathVariable ProductCode productCode
    ) {

        ProductChecklistResponse response = productChecklistService.getProductChecklist(productCode);
        return ResponseEntity.ok(response);
    }

}