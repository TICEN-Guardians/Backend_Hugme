package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PropertyResolveRequest(

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다.")
        String address

) {
}