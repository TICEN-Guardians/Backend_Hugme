package com.project.hugme.domain.diagnosis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PropertyResolveRequest(

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다.")
        String address,

        @NotBlank(message = "동 정보는 필수입니다.")
        @Size(max = 100, message = "동 정보는 100자 이하여야 합니다.")
        String dongName,

        @Size(
                min = 1,
                max = 100,
                message = "호 정보는 1자 이상 100자 이하여야 합니다."
        )
        String hoName
) {
}