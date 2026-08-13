package com.project.hugme.domain.checklist.repository;

import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationInfoRepository
        extends JpaRepository<ApplicationInfo, Long> {

    @Query("""
                SELECT applicationInfo
                FROM ApplicationInfo applicationInfo
                JOIN FETCH applicationInfo.housingType
                WHERE applicationInfo.application.applicationId = :applicationId
                  AND applicationInfo.application.user.userId = :userId
            """)
    Optional<ApplicationInfo> findByApplicationIdAndUserId(
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId
    );
}