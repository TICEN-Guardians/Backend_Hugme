package com.project.hugme.domain.checklist.service;


import com.project.hugme.domain.checklist.dto.application.ApplicationCreateRequest;
import com.project.hugme.domain.checklist.dto.application.ApplicationCreateResponse;
import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import com.project.hugme.domain.checklist.dto.application.OCRUpdateRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionListResponse;
import com.project.hugme.domain.checklist.dto.question.QuestionResponse;
import com.project.hugme.domain.checklist.entity.application.Application;
import com.project.hugme.domain.checklist.entity.application.ApplicationInfo;
import com.project.hugme.domain.checklist.entity.application.ApplicationStatus;
import com.project.hugme.domain.checklist.entity.application.ContractType;
import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.entity.product.HousingTypeCode;
import com.project.hugme.domain.checklist.entity.product.Product;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.question.Question;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import com.project.hugme.domain.checklist.repository.*;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.EmailVerificationRequiredException;
import com.project.hugme.domain.user.exception.UserNotFoundException;
import com.project.hugme.domain.user.exception.WithdrawnUserException;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final QuestionRepository questionRepository;

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

    public QuestionListResponse getQuestions(Long userId, Long applicationId, QuestionStep questionStep) {

        //1. 로그인 사용자의 신청정보와 OCR 결과를 조회
        ApplicationInfo applicationInfo = applicationInfoRepository.findByApplicationIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "신청정보를 찾을 수 없습니다."
                ));

        //2. step은 optionId에 따라 달라져서 이후 다른 api로 진행
        if (questionStep == QuestionStep.STEP3) {
            throw new IllegalArgumentException(
                    "STEP3 질문은 답변 제출 결과로 조회해야 합니다."
            );
        }

        //3. 신청한 상품 코드를 가져온다.
        Product product = applicationInfo.getApplication()
                .getProduct();

        ProductCode productCode = product.getProductCode();

        //4. 특례반환보증 신규계약은 보증료 할인 질문을 하지 않음
        if (productCode == ProductCode.SPECIAL
                && applicationInfo.getContractType()
                == ContractType.NEW
                && questionStep == QuestionStep.STEP2) {

            List<QuestionResponse> emptyQuestions = new ArrayList<>();

            return new QuestionListResponse(questionStep, emptyQuestions);
        }

        //5. 상품과 질문 단계에 맞는 질문 조회
        List<Question> questions = questionRepository.findQuestions(
                productCode, questionStep);

        //최종질문목록이 될 List
        List<QuestionResponse> questionResponses =
                new ArrayList<>();

        //6.조회된 질문을 하나씩 확인한다.
        for (Question question : questions) {
            // OCR 임차인,임대인 유형 확인
            boolean partyTypeVisible = isPartyTypeVisible(question, applicationInfo);

            if (!partyTypeVisible) {
                continue;
            }

            //주택유형
            boolean housingTypeVisible = isHousingTypeVisible(
                    question,
                    applicationInfo
            );

            if (!housingTypeVisible) {
                continue;
            }

            QuestionResponse questionResponse = QuestionResponse.from(question);

            questionResponses.add(questionResponse);
        }
        return new QuestionListResponse(
                questionStep,
                questionResponses
        );


    }

    private boolean isHousingTypeVisible(Question question, ApplicationInfo applicationInfo) {

        HousingTypeCode housingTypeCode = applicationInfo.getHousingType().getHousingTypeCode();

        String questionText = question.getQuestionText();

        /*
         * 신규 분양 아파트 질문은
         * APARTMENT에서만 표시한다.
         */
        if (questionText.equals(
                "소유권보존등기만 이루어진 신규 분양 아파트인가요?"
        )) {
            return housingTypeCode
                    == HousingTypeCode.APARTMENT;
        }

        /*
         * 상가 확인 질문은
         * 단독·다중·다가구주택에서만 표시한다.
         */
        if (questionText.equals(
                "건축물대장에 근린생활시설 등 상가가 확인되나요?"
        )) {
            return housingTypeCode
                    == HousingTypeCode.HOUSE;
        }

        /*
         * 별도의 주택유형 제한이 없는 질문은 표시한다.
         */
        return true;


    }

    private boolean isPartyTypeVisible(Question question, ApplicationInfo applicationInfo) {
        if (question.getTenantType() != null
                && question.getTenantType()
                != applicationInfo.getTenantType()) {

            return false;
        }


        if (question.getLandlordType() != null
                && question.getLandlordType()
                != applicationInfo.getLandlordType()) {

            return false;
        }

        return true;

    }
}
