package com.project.hugme.domain.checklist.service;


import com.project.hugme.domain.checklist.dto.application.ApplicationCreateRequest;
import com.project.hugme.domain.checklist.dto.application.ApplicationCreateResponse;
import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import com.project.hugme.domain.checklist.dto.application.OCRUpdateRequest;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ApplicationStatus;
import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.entity.product.Product;
import com.project.hugme.domain.checklist.repository.ApplicationInfoRepository;
import com.project.hugme.domain.checklist.repository.ApplicationRepository;
import com.project.hugme.domain.checklist.repository.HousingTypeRepository;
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
    private final ApplicationInfoRepository applicationInfoRepository;
    private final HousingTypeRepository housingTypeRepository;


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


    public OCRResponse getOCRResult(Long userId, Long applicationId) {
// userID applicationId
        ApplicationInfo applicationInfo =
                applicationInfoRepository
                        .findByApplicationIdAndUserId(
                                applicationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OCR 결과를 찾을 수 없습니다."
                                )
                        );

        return OCRResponse.from(applicationInfo);
    }

    @Transactional
    public OCRResponse updateOCRResult(Long userId, Long applicationId, OCRUpdateRequest request
    ) {
        ApplicationInfo applicationInfo =
                applicationInfoRepository
                        .findByApplicationIdAndUserId(
                                applicationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OCR 결과를 찾을 수 없습니다."
                                )
                        );
        HousingType housingType =
                housingTypeRepository
                        .findByHousingTypeCode(
                                request.housingTypeCode()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "주택유형을 찾을 수 없습니다: "
                                                + request.housingTypeCode()
                                )
                        );

        applicationInfo.updateAndConfirm(
                housingType,
                request.contractAddress(),
                request.contractType(),
                request.tenantType(),
                request.landlordType(),
                request.fixedDateConfirmed(),
                request.officetelResidentialMarked(),
                request.landlordProxyContract()
        );

        return OCRResponse.from(
                applicationInfo
        );
   
    }
}
