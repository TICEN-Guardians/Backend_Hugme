package com.project.hugme.domain.checklist.dto.application;

import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record CompletedApplicationResponse(
        Long applicationId,
        String contractAddress,
        LocalDate updatedAt
) {
    public static CompletedApplicationResponse from(
            Application application
    ) {
        ApplicationInfo applicationInfo =
                application.getApplicationInfo();

        LocalDate updatedDate =
                applicationInfo
                        .getUpdatedAt()
                        .atZone(
                                ZoneId.of("Asia/Seoul")
                        )
                        .toLocalDate();

        return new CompletedApplicationResponse(
                application.getApplicationId(),
                applicationInfo.getContractAddress(),
                updatedDate
        );
    }
}