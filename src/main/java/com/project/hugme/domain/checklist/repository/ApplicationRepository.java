package com.project.hugme.domain.checklist.repository;


import com.project.hugme.domain.checklist.entity.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query(
            value = """
                    SELECT *
                    FROM applications
                    WHERE user_id = :userId
                      AND product_id = :productId
                      AND application_status = 'DONE'
                    ORDER BY created_at DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<Application> findLatestCompletedApplication(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );


    Optional<Application> findByApplicationIdAndUser_UserId(Long applicationId, Long userId);

    Optional<Application> findByUser_UserId(Long userId);
}
