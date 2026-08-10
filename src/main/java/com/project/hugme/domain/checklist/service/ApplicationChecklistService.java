package com.project.hugme.domain.checklist.service;


import com.project.hugme.domain.checklist.dto.application.ApplicationCreateRequest;
import com.project.hugme.domain.checklist.dto.application.ApplicationCreateResponse;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationStatus;
import com.project.hugme.domain.checklist.entity.product.Product;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.checklist.repository.ProductRepository;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.EmailVerificationRequiredException;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.exception.WithdrawnUserException;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationChecklistService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Transactional
    public ApplicationCreateResponse createApplication(Long userId, ApplicationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getStatus() == UserStatus.PENDING) {
            throw new EmailVerificationRequiredException();
        }

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new WithdrawnUserException();
        }

        Product product = productRepository
                .findByProductCode(request.productCode())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다: "
                                        + request.productCode()
                        )
                );

        List<Application> completedApplications =
                applicationRepository.findApplications(
                        user.getUserId(),
                        product.getProductId(),
                        ApplicationStatus.DONE
                );

        if (!completedApplications.isEmpty()) {

            /*
             * createdAt 내림차순으로 정렬했으므로
             * 0번이 가장 최근에 완료된 신청이다.
             */
            Application completedApplication =
                    completedApplications.get(0);

            return ApplicationCreateResponse.from(
                    completedApplication
            );
        }
        Application newApplication = Application.create(user, product);

        Application savedApplication = applicationRepository.save(newApplication);

        return ApplicationCreateResponse.from(
                savedApplication
        );

    }
}
