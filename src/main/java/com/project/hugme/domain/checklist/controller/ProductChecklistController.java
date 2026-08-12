package com.project.hugme.domain.checklist.controller;

import com.project.hugme.domain.checklist.dto.product.ItemDocumentsResponse;
import com.project.hugme.domain.checklist.dto.product.ProductChecklistResponse;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.product.SectionCode;
import com.project.hugme.domain.checklist.service.ProductChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(
        name = "상품별 준비서류 API",
        description = "로그인 없이 보증상품별 전체 체크리스트와 제출서류를 조회하는 API"
)
public class ProductChecklistController {

    private final ProductChecklistService productChecklistService;


    @SecurityRequirements
    @Operation(
            summary = "상품별 준비 항목 조회",
            description = """
                    상품 코드와 서류 구분을 기준으로 준비 항목 목록을 조회합니다.
                    BASIC은 기본서류, ADDITIONAL은 추가서류,
                    DISCOUNT는 보증료 할인서류입니다.
                    """
    )
    @GetMapping("/{productCode}/checklist")
    public ResponseEntity<ProductChecklistResponse> getProductChecklist(
            @PathVariable ProductCode productCode,
            @RequestParam SectionCode sectionCode
    ) {
        ProductChecklistResponse response =
                productChecklistService
                        .getProductChecklist(
                                productCode,
                                sectionCode
                        );

        return ResponseEntity.ok(response);
    }


    @SecurityRequirements
    @Operation(
            summary = "준비 항목별 실제 서류 조회",
            description = "선택한 준비 항목에 속한 실제 제출서류를 조회합니다."
    )
    @GetMapping(
            "/{productCode}/checklist/items/{itemId}/documents"
    )
    public ResponseEntity<ItemDocumentsResponse> getItemDocuments(
            @PathVariable ProductCode productCode,
            @PathVariable Long itemId
    ) {
        ItemDocumentsResponse response =
                productChecklistService
                        .getItemDocuments(
                                productCode,
                                itemId
                        );

        return ResponseEntity.ok(response);
    }

}