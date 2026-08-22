package com.project.hugme.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record NotificationSendRequest(

        @NotBlank(message = "카카오 인가 코드는 필수입니다.")
        String code,

        @NotBlank(message = "state 값은 필수입니다.")
        String state,

        @NotNull(message = "계약 시작일은 필수입니다.")
        LocalDate contractStartDate,

        @NotNull(message = "계약 종료일은 필수입니다.")
        LocalDate contractEndDate,

        @NotNull(message = "잔금지급일은 필수입니다.")
        LocalDate balancePaymentDate,

        @NotNull(message = "전입신고일은 필수입니다.")
        LocalDate moveInDate
) {
}