package com.project.hugme.domain.checklist.repository;


import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("""
                SELECT application
                FROM Application application
                WHERE application.user.userId = :userId
                  AND application.product.productId = :productId
                  AND application.applicationStatus = :applicationStatus
                ORDER BY application.createdAt DESC
            """)
    List<Application> findApplications(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("applicationStatus")
            ApplicationStatus applicationStatus
    );

}
