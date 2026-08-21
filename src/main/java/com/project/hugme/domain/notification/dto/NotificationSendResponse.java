package com.project.hugme.domain.notification.dto;

import java.time.LocalDate;

public record NotificationSendResponse(
        Long applicationId,
        LocalDate applicationDeadline,
        long dDay,
        boolean sent
) {
}