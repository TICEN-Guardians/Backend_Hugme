package com.project.hugme.domain.checklist.service;


import com.project.hugme.domain.checklist.dto.application.ApplicationCreateRequest;
import com.project.hugme.domain.checklist.dto.application.ApplicationCreateResponse;
import com.project.hugme.domain.checklist.dto.application.OCRResponse;
import com.project.hugme.domain.checklist.dto.application.OCRUpdateRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersRequest;
import com.project.hugme.domain.checklist.dto.question.QuestionAnswersResponse;
import com.project.hugme.domain.checklist.dto.question.QuestionListResponse;
import com.project.hugme.domain.checklist.dto.question.QuestionResponse;
import com.project.hugme.domain.checklist.entity.application.*;
import com.project.hugme.domain.checklist.entity.product.HousingType;
import com.project.hugme.domain.checklist.entity.product.HousingTypeCode;
import com.project.hugme.domain.checklist.entity.product.Product;
import com.project.hugme.domain.checklist.entity.product.ProductCode;
import com.project.hugme.domain.checklist.entity.question.Question;
import com.project.hugme.domain.checklist.entity.question.QuestionOption;
import com.project.hugme.domain.checklist.entity.question.QuestionStep;
import com.project.hugme.domain.checklist.repository.*;
import com.project.hugme.domain.user.entity.User;
import com.project.hugme.domain.user.entity.UserStatus;
import com.project.hugme.domain.user.exception.EmailVerificationRequiredException;
import com.project.hugme.domain.user.exception.WithdrawnUserException;
import com.project.hugme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.project.hugme.domain.checklist.entity.product.ProductCode.SPECIAL;

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
    private final QuestionOptionRepository questionOptionRepository;
    private final FinalDocumentService finalDocumentService;

    @Transactional
    public ApplicationCreateResponse createApplication(Long userId, ApplicationCreateRequest request) {

        //Application 엔티티를 만들기 위해 필요한 User 엔티티를 조회
        //유저ID를 바탕으로 유저 상태 확인 - 이메인 인증전인지,탈퇴한 회원인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new EmailVerificationRequiredException();
        }

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new WithdrawnUserException();
        }

        //Application 엔티티를 만들기 위해 필요한 Product 엔티티를 조회
        Product product = productRepository
                .findByProductCode(request.productCode())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다: "
                                        + request.productCode()
                        )
                );

//        Optional<Application> existingApplication =
//                applicationRepository.findByUser_UserId(userId);

//        if (existingApplication.isPresent()) {
//            Application application = existingApplication.get();
//
//            applicationRepository.delete(application);
//
//            // 자식 데이터와 기존 Application 삭제 SQL을 즉시 실행
//            applicationRepository.flush();
//        }
        //새로운 Application 엔티티 생성
        Application newApplication = Application.create(user, product);

        //Application 엔티티 저장
        Application savedApplication = applicationRepository.save(newApplication);

        return ApplicationCreateResponse.from(
                savedApplication
        );

    }
    @Transactional
    public ApplicationCreateResponse prepareApplication(Long userId, ApplicationCreateRequest request) {

        //Application 엔티티를 만들기 위해 필요한 User 엔티티를 조회
        //유저ID를 바탕으로 유저 상태 확인 - 이메인 인증전인지,탈퇴한 회원인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다."));


        //Application 엔티티를 만들기 위해 필요한 Product 엔티티를 조회
        Product product = productRepository
                .findByProductCode(request.productCode())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다: "
                                        + request.productCode()
                        )
                );

        // 모의테스트 기본 주택유형
        HousingType housingType =
                housingTypeRepository
                        .findByHousingTypeCode(
                                HousingTypeCode.APARTMENT
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "아파트 주택유형이 없습니다."
                                )
                        );

        // READY 상태 Application 생성
        Application application =
                Application.prepare(
                        user,
                        product
                );

        Application savedApplication =
                applicationRepository.save(application);

        // OCR 대신 기본값으로 ApplicationInfo 생성
        ApplicationInfo applicationInfo =
                ApplicationInfo.create(
                        savedApplication
                );

        applicationInfo.updateAndConfirm(
                housingType,
                "모의테스트용 주소",
                ContractType.NEW,
                PartyType.PERSON,
                PartyType.PERSON,
                false,
                false,
                false
        );

        applicationInfoRepository.save(
                applicationInfo
        );

        return ApplicationCreateResponse.from(
                savedApplication
        );

    }




    public ApplicationCreateResponse getCurrentApplication(Long userId,ProductCode productCode) {

        Product product =
                productRepository
                        .findByProductCode(productCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 상품입니다: "
                                                + productCode
                                )
                        );

        Application application =
                applicationRepository
                        .findLatestCompletedApplication(
                                userId,
                                product.getProductId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "완료된 이전 체크리스트가 없습니다."
                                )
                        );

        return ApplicationCreateResponse.from(
                application
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

        //로그인 사용자의 신청정보와 OCR 결과를 조회
        ApplicationInfo applicationInfo = applicationInfoRepository.findByApplicationIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "신청정보를 찾을 수 없습니다."
                ));

        //step3은 optionId에 따라 달라져서 이후 다른 api로 진행
        if (questionStep == QuestionStep.STEP3) {
            throw new IllegalArgumentException(
                    "STEP3 질문은 답변 제출 결과로 조회해야 합니다."
            );
        }
        //질문 처리

        //신청한 상품 코드를 가져온다.
        Product product = applicationInfo.getApplication()
                .getProduct();

        ProductCode productCode = product.getProductCode();

        //특례반환보증 신규계약은 보증료 할인 질문을 하지 않음
        if (productCode == SPECIAL
                && applicationInfo.getContractType()
                == ContractType.NEW
                && questionStep == QuestionStep.STEP2) {

            List<QuestionResponse> emptyQuestions = new ArrayList<>();

            return new QuestionListResponse(questionStep, emptyQuestions);
        }

        //상품과 질문 단계에 맞는 질문 조회
        List<Question> questions = questionRepository.findQuestions(
                productCode, questionStep);

        //최종질문목록 = List + QuestionStep
        List<QuestionResponse> questionResponses =
                new ArrayList<>();

        //조회된 질문을 하나씩 확인한다.
        for (Question question : questions) {
            // OCR 임차인,임대인 유형 확인
            boolean partyTypeVisible = isPartyTypeVisible(question, applicationInfo);

            if (!partyTypeVisible) {
                continue;
            }

            //주택유형
            boolean housingTypeVisible = isVisibleForHousingType(
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

    private boolean isVisibleForHousingType(Question question, ApplicationInfo applicationInfo) {

        HousingTypeCode housingTypeCode = applicationInfo.getHousingType().getHousingTypeCode();

        String questionText = question.getQuestionText();


        //신규 분양 아파트 질문은
        //APARTMENT에서만 표시한다.
        if (questionText.equals(
                "소유권보존등기만 이루어진 신규 분양 아파트인가요?"
        )) {
            return housingTypeCode
                    == HousingTypeCode.APARTMENT;
        }


        //상가 확인 질문은
        //단독·다중·다가구주택에서만 표시한다.
        if (questionText.equals(
                "건축물대장에 근린생활시설 등 상가가 확인되나요?"
        )) {
            return housingTypeCode
                    == HousingTypeCode.HOUSE;
        }


        //별도의 주택유형 제한이 없는 질문은 표시한다.
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

    @Transactional
    public QuestionAnswersResponse submitAnswers(Long userId, Long applicationId, QuestionAnswersRequest request) {

        //신청자 정보 확인
        ApplicationInfo applicationInfo = applicationInfoRepository.findByApplicationIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "신청정보를 찾을 수 없습니다."
                ));

        List<Long> selectedOptionIds = request.selectedOptionIds();

        /*
         *  3. optionIds를 기준으로 QuestionOption과 연결된 childQuestions를 함께 조회
         */
        List<QuestionOption> selectedOptions = questionOptionRepository.findAllWithChildQuestions(selectedOptionIds);


        /*
         * 4. option이 실제 questionId에 속한 선택지인지 검증한다.
         */
        if (selectedOptions.size() != selectedOptionIds.size()) {
            throw new IllegalArgumentException(
                    "존재하지 않는 선택지가 포함되어 있습니다."
            );
        }

        /*
         * 5. 선택지를 이용해 이미 답변한 questionId를 구한다.
         *
         * questionId는 프론트에서 받지 않고
         * QuestionOption.question 관계를 통해 확인한다.
         */
        List<Long> answeredQuestionIds = new ArrayList<>();

        for (QuestionOption selectedOption : selectedOptions) {
            Long questionId = selectedOption.getQuestion().getQuestionId();

            if (!answeredQuestionIds.contains(questionId)) {
                answeredQuestionIds.add(questionId);
            }
        }


        //entity를 dto로 변환
        List<QuestionResponse> additionalQuestionResponses = new ArrayList<>();

        List<Question> additionalQuestions = new ArrayList<>();

        for (QuestionOption selectedOption : selectedOptions) {

            Set<Question> childQuestions = selectedOption.getChildQuestions();

            for (Question childQuestion : childQuestions) {
                Long childQuestionId = childQuestion.getQuestionId();
                //이미 기존의 질문 내역에 있다면
                if (answeredQuestionIds.contains(childQuestionId)) {
                    continue;
                }
                //임차인/임대인 유형에 맞지 않으면 제외
                if (!isPartyTypeVisible(childQuestion, applicationInfo)) {
                    continue;
                }
                //같은 질문이 있는지 확인

                boolean alreadyAdded = false;

                for (Question addedQuestion : additionalQuestions) {
                    if (addedQuestion.getQuestionId().equals(childQuestionId)) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    additionalQuestions.add(childQuestion);
                }


            }


        }
        for (Question additionalQuestion : additionalQuestions) {
            QuestionResponse questionResponse = QuestionResponse
                    .from(additionalQuestion);

            additionalQuestionResponses.add(questionResponse);
        }

// 추가 질문이 있는경우

        //GENERAL,SPECIAL,SAFE_LOAN
        if (request.currentStep() == QuestionStep.STEP1
                && isStep2Available(applicationInfo)) {
            return new QuestionAnswersResponse(
                    request.currentStep(),
                    QuestionStep.STEP2,
                    false,
                    additionalQuestionResponses
            );
        }
        if (!additionalQuestions.isEmpty()) {
            return new QuestionAnswersResponse(
                    request.currentStep(),
                    QuestionStep.STEP3,
                    false,
                    additionalQuestionResponses
            );

        }

        //최종제출이 아니면 서류를 저장하지 않는다.

        if (!request.finalSubmission()) {

            return new QuestionAnswersResponse(
                    request.currentStep(),
                    null,
                    false,
                    new ArrayList<>()
            );
        }

        //최종제출이고 남은 step3도 없다면 최종서류 계산
        finalDocumentService.rebuild(
                applicationInfo,
                selectedOptions
        );

        Application application =
                applicationInfo.getApplication();

        if (application.getApplicationStatus()
                == ApplicationStatus.PROGRESS) {

            application.complete();
        }

        return new QuestionAnswersResponse(
                request.currentStep(),
                null,
                true,
                additionalQuestionResponses
        );

    }


    private boolean isStep2Available(
            ApplicationInfo applicationInfo
    ) {
        ProductCode productCode = applicationInfo
                .getApplication()
                .getProduct()
                .getProductCode();

        ContractType contractType = applicationInfo.getContractType();

        if (productCode == ProductCode.GENERAL) {
            return true;
        }
        if (productCode == ProductCode.SPECIAL && contractType == ContractType.RENEWAL) {
            return true;
        }
        return false;
    }


}
