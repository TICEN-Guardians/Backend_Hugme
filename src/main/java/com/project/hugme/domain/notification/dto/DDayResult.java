package com.project.hugme.domain.notification.dto;

import java.time.LocalDate;

public record DDayResult(
        LocalDate applicationDeadline,
        long dDay
) {
}