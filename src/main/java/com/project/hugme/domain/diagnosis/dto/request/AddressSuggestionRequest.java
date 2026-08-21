package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressSuggestionRequest(
        @NotBlank(message = "주소 검색어는 필수입니다.")
        @Size(min = 2, max = 500, message = "주소 검색어는 2자 이상 500자 이하여야 합니다.")
        String address
) {
}
